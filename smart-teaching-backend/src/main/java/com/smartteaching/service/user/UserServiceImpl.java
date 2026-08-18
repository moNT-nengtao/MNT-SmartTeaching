package com.smartteaching.service.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.UserAddDTO;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.vo.UserQueryVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

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
    public void addUser(UserAddDTO dto, MultipartFile avatarFile) {
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
}
