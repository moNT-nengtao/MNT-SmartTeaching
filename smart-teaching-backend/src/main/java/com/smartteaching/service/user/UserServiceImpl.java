package com.smartteaching.service.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.UserExcelDTO;
import com.smartteaching.common.dto.UserSaveDTO;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.utils.UserExcelValidateUtil;
import com.smartteaching.common.vo.UserQueryVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.OrgMapper;
import com.smartteaching.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgMapper orgMapper;

    // yml读取头像存放相对目录
    @Value("${file.avatar.relative-path}")
    private String avatarRelativePath;

    // 默认头像地址
    private static final String DEFAULT_AVATAR = "/avatar/default.png";

    /**
     * 用户分页查询
     */
    @Override
    public PageResult<UserQueryVO> pageQuery(UserQueryDTO userQueryDTO) {
        long pageNum = userQueryDTO.getPage() == null ? 1 : userQueryDTO.getPage();
        long pageSize = userQueryDTO.getPageSize() == null ? 10 : userQueryDTO.getPageSize();

        IPage<UserQueryVO> mpPage = new Page<>(pageNum, pageSize);
        IPage<UserQueryVO> voIPage = userMapper.selectUserPage(mpPage, userQueryDTO);

        return PageResult.build(
                voIPage.getTotal(),
                voIPage.getPages(),
                voIPage.getCurrent(),
                voIPage.getSize(),
                voIPage.getRecords()
        );
    }

    /**
     * 新增用户，支持上传头像
     * 事务要点：数据库先插入，事务提交后再写磁盘文件；写文件失败就删除数据库记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserSaveDTO dto, MultipartFile avatarFile) {
        User user = new User();
        BeanUtil.copyProperties(dto, user, false);
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        String avatarUrl;
        String finalFileName = null;
        byte[] avatarBytes = null;

        // 判断头像状态
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String originalFilename = avatarFile.getOriginalFilename();
            String suffix = "jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            // 生成唯一文件名
            finalFileName = dto.getUsername() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + suffix;
            avatarUrl = "/avatar/" + finalFileName;
            user.setAvatar(avatarUrl);
            try {
                avatarBytes = avatarFile.getBytes();
            } catch (IOException e) {
                log.error("读取头像字节失败", e);
                throw new RuntimeException(MessageConstant.AVATAR_SAVE_IO_ERROR, e);
            }
        } else {
            // 使用默认头像
            user.setAvatar(DEFAULT_AVATAR);
        }

        // 插入数据库
        userMapper.insert(user);
        Long userId = user.getId();

        // 事务提交完成后，再把头像写入磁盘
        if (avatarBytes != null && finalFileName != null) {
            byte[] finalAvatarBytes = avatarBytes;
            String saveFileName = finalFileName;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    String projectBase = System.getProperty("user.dir");
                    File baseDir = new File(projectBase, avatarRelativePath);
                    File destFile = new File(baseDir, saveFileName);
                    // 创建存放头像的文件夹
                    if (!baseDir.exists()) {
                        boolean created = baseDir.mkdirs();
                        if (!created) {
                            log.error("{}：{}", MessageConstant.AVATAR_DIR_CREATE_FAILED, baseDir.getAbsolutePath());
                            userMapper.deleteById(userId);
                            throw new RuntimeException(MessageConstant.AVATAR_DIR_CREATE_FAILED);
                        }
                    }

                    log.info("头像最终存储完整路径：{}", destFile.getAbsolutePath());

                    // 写出头像文件到磁盘
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        fos.write(finalAvatarBytes);
                        log.info("用户头像保存成功");
                    } catch (IOException e) {
                        log.error(MessageConstant.AVATAR_SAVE_IO_ERROR, e);
                        userMapper.deleteById(userId);
                        throw new RuntimeException(MessageConstant.ADD_USER_AVATAR_FAIL_ROLLBACK, e);
                    }
                }
            });
        }
    }

    /**
     * 编辑用户
     *
     * @param userSaveDTO
     * @param avatarFile
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserSaveDTO userSaveDTO, MultipartFile avatarFile) {
        User user = userMapper.selectById(userSaveDTO.getId());
        String oldAvatarPath = user.getAvatar();
        BeanUtil.copyProperties(userSaveDTO, user, CopyOptions.create()
                .setIgnoreNullValue(false)
                .setIgnoreProperties("password"));
        user.setUpdateTime(LocalDateTime.now());

        if(userSaveDTO.getPassword() != null && !userSaveDTO.getPassword().isBlank()) {
            user.setPassword(DigestUtils.md5DigestAsHex(userSaveDTO.getPassword().getBytes()));
        }


        String finalFileName = null;
        byte[] newAvatarBytes = null;
        String newAvatarUrl = null;

        //判断头像上传
        if(avatarFile !=  null && !avatarFile.isEmpty() ) {
            String originalFilename = avatarFile.getOriginalFilename();
            String suffix = "jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }
            finalFileName = userSaveDTO.getUsername() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + suffix;
            newAvatarUrl = "/avatar/" + finalFileName;
            user.setAvatar(newAvatarUrl);
            try {
                newAvatarBytes = avatarFile.getBytes();
            } catch (IOException e) {
                log.error(MessageConstant.AVATAR_SAVE_IO_ERROR, e);
                throw new RuntimeException(MessageConstant.AVATAR_SAVE_IO_ERROR, e);
            }
        }
        //更新字段
        userMapper.updateById(user);
        Long userId = user.getId();

        //传入头像
        if (newAvatarBytes != null && finalFileName != null) {
            byte[] finalAvatarBytes = newAvatarBytes;
            String saveFileName = finalFileName;
            String finalOldAvatar = oldAvatarPath;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    String projectBase = System.getProperty("user.dir");
                    File baseDir = new File(projectBase, avatarRelativePath);
                    File destFile = new File(baseDir, saveFileName);

                    if (!baseDir.exists()) {
                        boolean created = baseDir.mkdirs();
                        if (!created) {
                            log.error("{}：{}", MessageConstant.AVATAR_DIR_CREATE_FAILED, baseDir.getAbsolutePath());
                            //失败则恢复旧头像
                            restoreUserAvatar(userId,finalOldAvatar);
                            throw new RuntimeException(MessageConstant.AVATAR_DIR_CREATE_FAILED);
                        }
                    }
                    log.info("更新用户，新头像存储完整路径：{}", destFile.getAbsolutePath());

                    try (FileOutputStream fos = new FileOutputStream(destFile)){
                        fos.write(finalAvatarBytes);
                        log.info(MessageConstant.AVATAR_WRITE_SUCCESS);
                    }catch (IOException e){
                        log.error(MessageConstant.AVATAR_SAVE_IO_ERROR,e);
                        //写文件失败，恢复数据库头像为旧头像
                        restoreUserAvatar(userId, finalOldAvatar);
                        throw new RuntimeException(MessageConstant.ADD_USER_AVATAR_FAIL_ROLLBACK,e);
                    }
                    //更新头像成功
                    if(finalOldAvatar != null
                            && !finalOldAvatar.isBlank()
                            && !DEFAULT_AVATAR.equals(finalOldAvatar)
                            && finalOldAvatar.startsWith("/avatar/")){
                        //截取文件名
                        String oldFileName = finalOldAvatar.substring("/avatar/".length());
                        File oldFile = new File(baseDir, oldFileName);
                        if(oldFile.exists()){
                            boolean del = oldFile.delete();
                            if(!del){
                                log.warn("旧头像文件删除失败，文件路径：{}", oldFile.getAbsolutePath());
                            }else{
                                log.info("旧头像已删除：{}",oldFile.getName());
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * 删除用户
     *
     * @param id
     * @param loginUserId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String id, Long loginUserId) {
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("待删除用户已不存在，id={}", id);
            return;
        }

        //效验
        if (user.getId().equals(loginUserId)) {
            throw new BaseException(MessageConstant.CANNOT_DELETE_SELF);
        }
        if ("admin".equals(user.getRole())) {
            throw new BaseException(MessageConstant.CANNOT_DELETE_ADMIN);
        }

        String avatar = user.getAvatar();
        //删数据库
        int rows = userMapper.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException(MessageConstant.DELETE_USER_FAIL);
        }
        //删头像
        if(avatar != null && !avatar.isBlank() && !DEFAULT_AVATAR.equals(avatar)){
            String fileName = avatar.substring("/avatar/".length());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteAvatarFile(fileName);
                }
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        log.info("删除用户事务回滚，不删除头像文件，ID: {}", id);
                    }
                    }
                }
            );
        }
                    log.info("用户删除成功，ID: {}, 用户名: {}", id, user.getUsername());
    }

    /**
     * 启用/禁用用户
     * @param id
     * @param loginUserId
     */
    @Override
    public void statusUser(String id, Long loginUserId) {
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn(MessageConstant.OPERATE_USER_NOT_EXIST);
            return;
        }

        if (id.equals(loginUserId)) {
            throw new BaseException(MessageConstant.CANNOT_DISABLE_SELF);
        }
        if ("admin".equals(user.getRole())) {
            throw new BaseException(MessageConstant.CANNOT_DISABLE_ADMIN);
        }

        //切换状态
        Integer newStatus = user.getStatus() == 1 ? 0 : 1;

        user.setStatus(newStatus);
        userMapper.updateById(user);

    }

    /**
     * 批量导入用户
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImportUsers(MultipartFile file) {
        List<UserExcelDTO> validDataList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        List<User> validUsers = new ArrayList<>();

        try {
            EasyExcel.read(file.getInputStream(), UserExcelDTO.class, new ReadListener<UserExcelDTO>() {
                private int rowNum = 1;

                @Override
                public void invoke(UserExcelDTO data, AnalysisContext context) {
                    rowNum++;

                    List<String> errors = UserExcelValidateUtil.validateUser(
                            data.getUsername(),
                            data.getRealName(),
                            data.getPassword(),
                            data.getPhone(),
                            data.getEmail()
                    );

                    if (!errors.isEmpty()) {
                        errorList.add(UserExcelValidateUtil.formatError(rowNum, data.getUsername(), errors));
                    } else {
                        data.setRowNum(rowNum);
                        validDataList.add(data);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 解析完成，基础校验通过 {} 条，错误 {} 条",
                            validDataList.size(), errorList.size());
                }
            }).sheet().doRead();

        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new RuntimeException("读取 Excel 文件失败: " + e.getMessage());
        }

        if (!errorList.isEmpty()) {
            throw new BaseException(String.format("导入失败，共 %d 条错误：%s",
                    errorList.size(), String.join("；", errorList)));
        }

        // 批量校验组织名称
        List<UserExcelValidateUtil.OrgValidateResult> orgResults =
                UserExcelValidateUtil.batchValidateAndConvertOrgNames(validDataList, orgMapper);

        // 检查组织名称校验结果
        for (int i = 0; i < validDataList.size(); i++) {
            UserExcelDTO data = validDataList.get(i);
            UserExcelValidateUtil.OrgValidateResult orgResult = orgResults.get(i);

            if (!orgResult.isValid()) {
                // 使用保存的行号
                int rowNum = data.getRowNum();
                errorList.add(UserExcelValidateUtil.formatError(rowNum, data.getUsername(), orgResult.getErrors()));
            } else {
                User user = UserExcelValidateUtil.buildUser(
                        data,
                        DEFAULT_AVATAR,
                        orgResult.getCollegeId(),
                        orgResult.getMajorId(),
                        orgResult.getClassId()
                );
                validUsers.add(user);
            }
        }

        // 如果有组织名称错误，全部回滚
        if (!errorList.isEmpty()) {
            throw new BaseException(String.format("导入失败，共 %d 条错误：%s",
                    errorList.size(), String.join("；", errorList)));
        }

        // 批量插入用户
        int successCount = 0;
        for (User user : validUsers) {
            try {
                userMapper.insert(user);
                successCount++;
            } catch (DuplicateKeyException e) {
                throw new BaseException("账号 " + user.getUsername() + " 已存在");
            } catch (Exception e) {
                log.error("用户插入失败，账号: {}", user.getUsername(), e);
                throw new BaseException("用户插入失败: " + e.getMessage());
            }
        }

        log.info("批量导入完成，成功: {} 条", successCount);
        return successCount;
    }


    /**
     * 工具方法：头像IO异常，恢复数据库头像字段为旧值
     */
    private void restoreUserAvatar(Long userId, String oldAvatar){
        User update = new User();
        update.setId(userId);
        update.setAvatar(oldAvatar);
        update.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);
    }


    /**
     * 工具方法：删除头像文件
     *
     * @param fileName 文件名
     */
    private void deleteAvatarFile(String fileName) {
        try {
            String projectBase = System.getProperty("user.dir");
            File baseDir = new File(projectBase, avatarRelativePath);
            File avatarFile = new File(baseDir, fileName);

            if (avatarFile.exists() && avatarFile.isFile()) {
                boolean deleted = avatarFile.delete();
                if (deleted) {
                    log.info("头像文件删除成功: {}", fileName);
                } else {
                    log.warn("头像文件删除失败: {}", avatarFile.getAbsolutePath());
                }
            } else {
                log.warn("头像文件不存在，无需删除: {}", avatarFile.getAbsolutePath());
            }
        } catch (Exception e) {
            // 文件删除失败不影响用户删除的结果，只记录日志
            log.error("删除头像文件异常，文件: {}, 错误: {}", fileName, e.getMessage());
        }
    }


}