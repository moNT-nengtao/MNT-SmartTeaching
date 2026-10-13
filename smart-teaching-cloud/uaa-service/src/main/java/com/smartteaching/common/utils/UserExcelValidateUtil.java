package com.smartteaching.common.utils;

import com.smartteaching.common.dto.user.UserExcelDTO;
import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.OrgMapper;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Excel 导入校验工具类
 *
 * 方法：
 * validateUser                      - 校验用户基础字段
 * validateAndConvertOrgNames        - 单条校验组织名称→ID
 * batchValidateAndConvertOrgNames   - 批量校验组织名称→ID
 * formatError                       - 格式化错误信息
 * buildUser                         - 构建User对象
 *
 * 内部类：
 * OrgValidateResult     - 组织校验结果
 */
public class UserExcelValidateUtil {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 校验 Excel 导入的用户基础字段
     */
    public static List<String> validateUser(String username, String realName, String password,
                                            String phone, String email) {
        List<String> errors = new ArrayList<>();

        // 账号校验
        if (username == null || username.trim().isEmpty()) {
            errors.add("账号不能为空");
        } else {
            if (username.length() < 3 || username.length() > 20) {
                errors.add("账号长度3-20字符");
            }
            if (!USERNAME_PATTERN.matcher(username).matches()) {
                errors.add("账号仅字母数字下划线");
            }
        }

        // 密码校验
        if (password == null || password.trim().isEmpty()) {
            errors.add("密码不能为空");
        } else {
            if (password.length() < 6 || password.length() > 20) {
                errors.add("密码长度6-20位");
            }
        }

        // 姓名校验
        if (realName == null || realName.trim().isEmpty()) {
            errors.add("姓名不能为空");
        }

        // 手机号校验
        if (phone != null && !phone.isEmpty()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                errors.add("手机号格式错误");
            }
        }

        // 邮箱校验
        if (email != null && !email.isEmpty()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                errors.add("邮箱格式错误");
            }
        }

        return errors;
    }

    /**
     * 【单条版】校验单个用户的组织名称
     */
    public static OrgValidateResult validateAndConvertOrgNames(
            String collegeName,
            String majorName,
            String className,
            OrgMapper orgMapper) {

        OrgValidateResult result = new OrgValidateResult();
        List<String> errors = new ArrayList<>();

        // 校验学院
        if (collegeName == null || collegeName.trim().isEmpty()) {
            errors.add("学院名称不能为空");
        } else {
            Long id = orgMapper.getCollegeIdByName(collegeName.trim());
            if (id == null) {
                errors.add("学院 \"" + collegeName + "\" 不存在");
            } else {
                result.setCollegeId(id);
            }
        }

        // 校验专业
        if (majorName == null || majorName.trim().isEmpty()) {
            errors.add("专业名称不能为空");
        } else {
            Long id = orgMapper.getMajorIdByName(majorName.trim());
            if (id == null) {
                errors.add("专业 \"" + majorName + "\" 不存在");
            } else {
                result.setMajorId(id);
            }
        }

        // 校验班级
        if (className == null || className.trim().isEmpty()) {
            errors.add("班级名称不能为空");
        } else {
            Long id = orgMapper.getClassIdByName(className.trim());
            if (id == null) {
                errors.add("班级 \"" + className + "\" 不存在");
            } else {
                result.setClassId(id);
            }
        }

        result.setErrors(errors);
        return result;
    }

    /**
     * 【批量版】校验所有用户的组织名称（一次性查询数据库）
     */
    public static List<OrgValidateResult> batchValidateAndConvertOrgNames(
            List<UserExcelDTO> userList,
            OrgMapper orgMapper) {

        // 1. 收集所有需要查询的名称（去重）
        Set<String> collegeNames = new HashSet<>();
        Set<String> majorNames = new HashSet<>();
        Set<String> classNames = new HashSet<>();

        for (UserExcelDTO user : userList) {
            if (user.getCollegeName() != null && !user.getCollegeName().trim().isEmpty()) {
                collegeNames.add(user.getCollegeName().trim());
            }
            if (user.getMajorName() != null && !user.getMajorName().trim().isEmpty()) {
                majorNames.add(user.getMajorName().trim());
            }
            if (user.getClassName() != null && !user.getClassName().trim().isEmpty()) {
                classNames.add(user.getClassName().trim());
            }
        }

        // 2. 批量查询，构建 Name → ID 的 Map（使用实体类）
        Map<String, Long> collegeMap = new HashMap<>();
        if (!collegeNames.isEmpty()) {
            List<College> list = orgMapper.getCollegesByNames(new ArrayList<>(collegeNames));
            collegeMap = list.stream().collect(
                    Collectors.toMap(
                            College::getName,
                            College::getId,
                            (existing, replacement) -> existing  // 重名时保留第一个
                    )
            );
        }

        Map<String, Long> majorMap = new HashMap<>();
        if (!majorNames.isEmpty()) {
            List<Major> list = orgMapper.getMajorsByNames(new ArrayList<>(majorNames));
            majorMap = list.stream().collect(
                    Collectors.toMap(
                            Major::getName,
                            Major::getId,
                            (existing, replacement) -> existing
                    )
            );
        }

        Map<String, Long> classMap = new HashMap<>();
        if (!classNames.isEmpty()) {
            List<ClassInfo> list = orgMapper.getClassesByNames(new ArrayList<>(classNames));
            classMap = list.stream().collect(
                    Collectors.toMap(
                            ClassInfo::getName,
                            ClassInfo::getId,
                            (existing, replacement) -> existing
                    )
            );
        }

        // 3. 逐个校验，使用 Map 快速查找
        List<OrgValidateResult> results = new ArrayList<>();
        for (UserExcelDTO user : userList) {
            OrgValidateResult result = new OrgValidateResult();
            List<String> errors = new ArrayList<>();

            // 校验学院
            String collegeName = user.getCollegeName();
            if (collegeName == null || collegeName.trim().isEmpty()) {
                errors.add("学院名称不能为空");
            } else {
                String key = collegeName.trim();
                Long id = collegeMap.get(key);
                if (id == null) {
                    errors.add("学院 \"" + key + "\" 不存在");
                } else {
                    result.setCollegeId(id);
                }
            }

            // 校验专业
            String majorName = user.getMajorName();
            if (majorName == null || majorName.trim().isEmpty()) {
                errors.add("专业名称不能为空");
            } else {
                String key = majorName.trim();
                Long id = majorMap.get(key);
                if (id == null) {
                    errors.add("专业 \"" + key + "\" 不存在");
                } else {
                    result.setMajorId(id);
                }
            }

            // 校验班级
            String className = user.getClassName();
            if (className == null || className.trim().isEmpty()) {
                errors.add("班级名称不能为空");
            } else {
                String key = className.trim();
                Long id = classMap.get(key);
                if (id == null) {
                    errors.add("班级 \"" + key + "\" 不存在");
                } else {
                    result.setClassId(id);
                }
            }

            result.setErrors(errors);
            results.add(result);
        }

        return results;
    }

    /**
     * 组织名称转换结果封装类
     */
    public static class OrgValidateResult {
        private List<String> errors = new ArrayList<>();
        private Long collegeId;
        private Long majorId;
        private Long classId;

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public Long getCollegeId() {
            return collegeId;
        }

        public void setCollegeId(Long collegeId) {
            this.collegeId = collegeId;
        }

        public Long getMajorId() {
            return majorId;
        }

        public void setMajorId(Long majorId) {
            this.majorId = majorId;
        }

        public Long getClassId() {
            return classId;
        }

        public void setClassId(Long classId) {
            this.classId = classId;
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }

    /**
     * 格式化错误信息
     */
    public static String formatError(int rowNum, String username, List<String> errors) {
        String displayName = username != null && !username.isEmpty() ? username : "空账号";
        return String.format("第 %d 行 (%s) 错误: %s", rowNum, displayName, String.join("；", errors));
    }

    /**
     * 构建 User 对象
     */
    public static User buildUser(UserExcelDTO data, String defaultAvatar,
                                 Long collegeId, Long majorId, Long classId) {
        User user = new User();
        user.setUsername(data.getUsername());
        user.setRealName(data.getRealName());
        user.setPassword(DigestUtils.md5DigestAsHex(data.getPassword().getBytes()));
        user.setPhone(data.getPhone());
        user.setEmail(data.getEmail());
        user.setAvatar(defaultAvatar);
        user.setStatus(1);

        // 性别转换
        if ("男".equals(data.getGender())) {
            user.setGender(1);
        } else if ("女".equals(data.getGender())) {
            user.setGender(2);
        } else {
            user.setGender(0);
        }

        String role = data.getRole();
        user.setRole(role == null || role.isEmpty() ? "student" : role);

        // 设置三个组织 ID
        user.setCollegeId(collegeId);
        user.setMajorId(majorId);
        user.setClassId(classId);

        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        return user;
    }
}