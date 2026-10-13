# -*- coding: utf-8 -*-
"""MNT-SmartTeaching 微服务化 Feign 改造脚本"""
import io, sys, re
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

ROOT = r'C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud'
ok_count = 0
fail_list = []

def patch(path, old, new, count=1):
    """精确替换，校验出现次数"""
    global ok_count
    p = ROOT + '\\' + path
    with open(p, encoding='utf-8') as f:
        c = f.read()
    n = c.count(old)
    if n != count:
        fail_list.append((path, old[:60], 'expect %d got %d' % (count, n)))
        return
    c = c.replace(old, new)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)
    ok_count += 1
    print('OK  %s' % path)

# ==================== 1. api 模块：listCourses 增加 teacherId 参数 ====================
patch(r'api\src\main\java\com\smartteaching\api\client\CourseFeignClient.java',
"""    @GetMapping("/list")
    Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status);""",
"""    @GetMapping("/list")
    Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status,
                                           @RequestParam(value = "teacherId", required = false) Long teacherId);""")

patch(r'course-service\src\main\java\com\smartteaching\controller\internal\CourseInternalController.java',
"""    @GetMapping("/list")
    public Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        wrapper.orderByDesc(Course::getId);""",
"""    @GetMapping("/list")
    public Result<List<CourseInfoVO>> listCourses(@RequestParam(value = "status", required = false) Integer status,
                                                  @RequestParam(value = "teacherId", required = false) Long teacherId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Course::getStatus, status);
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        wrapper.orderByDesc(Course::getId);""")

# ==================== 2. ai-service：学生信息查询改 Feign ====================
patch(r'ai-service\src\main\java\com\smartteaching\service\ai\AiServiceImpl.java',
"""import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.AiAnalysisMapper;""",
"""import com.smartteaching.api.client.UserFeignClient;
import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.mapper.AiAnalysisMapper;""")

patch(r'ai-service\src\main\java\com\smartteaching\service\ai\AiServiceImpl.java',
"""import com.smartteaching.mapper.AiSessionMapper;
import com.smartteaching.mapper.UserMapper;
import jakarta.annotation.Resource;""",
"""import com.smartteaching.mapper.AiSessionMapper;
import jakarta.annotation.Resource;""")

patch(r'ai-service\src\main\java\com\smartteaching\service\ai\AiServiceImpl.java',
"""    @Resource
    private UserMapper userMapper;""",
"""    @Resource
    private UserFeignClient userFeignClient;""")

patch(r'ai-service\src\main\java\com\smartteaching\service\ai\AiServiceImpl.java',
"""        User student = userMapper.selectById(studentId);
        if (student == null || !"student".equals(student.getRole())) {
            throw new BaseException("学生不存在或不是学生角色");
        }""",
"""        UserInfoVO student = userFeignClient.getUserById(studentId).getData();
        if (student == null || !"student".equals(student.getRole())) {
            throw new BaseException("学生不存在或不是学生角色");
        }""")

# ==================== 3. analysis-service：教师信息查询改 Feign ====================
patch(r'analysis-service\src\main\java\com\smartteaching\service\dashboard\DashboardServiceImpl.java',
"""import com.smartteaching.mapper.DashboardMapper;
import com.smartteaching.mapper.UserMapper;""",
"""import com.smartteaching.api.client.UserFeignClient;
import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.mapper.DashboardMapper;""")

patch(r'analysis-service\src\main\java\com\smartteaching\service\dashboard\DashboardServiceImpl.java',
"""    private UserMapper userMapper;""",
"""    private UserFeignClient userFeignClient;""")

patch(r'analysis-service\src\main\java\com\smartteaching\service\dashboard\DashboardServiceImpl.java',
"""        User user = userMapper.selectById(teacherId);
        if (user == null || !"teacher".equals(user.getRole())) {
            throw new BaseException("非教师用户");
        }""",
"""        UserInfoVO user = userFeignClient.getUserById(teacherId).getData();
        if (user == null || !"teacher".equals(user.getRole())) {
            throw new BaseException("非教师用户");
        }""")

# ==================== 4. selection-score-service：课程查询改 Feign ====================
patch(r'selection-score-service\src\main\java\com\smartteaching\service\selection\SelectionServiceImpl.java',
"""import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.ScoreMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.mapper.ScoreMapper;""")

patch(r'selection-score-service\src\main\java\com\smartteaching\service\selection\SelectionServiceImpl.java',
"""    @Autowired
    private CourseMapper courseMapper;""",
"""    @Autowired
    private CourseFeignClient courseFeignClient;""")

patch(r'selection-score-service\src\main\java\com\smartteaching\service\selection\SelectionServiceImpl.java',
"""        // 检查课程容量是否已满
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BaseException("课程不存在");
        }

        // 获取课程教师ID
        Long teacherId = courseMapper.selectById(courseId).getTeacherId();
        if (teacherId == null) {
            throw new BaseException("课程已不存在");
        }""",
"""        // 检查课程是否存在（跨服务 Feign 调用 course-service）
        CourseInfoVO course = courseFeignClient.getCourseById(courseId).getData();
        if (course == null) {
            throw new BaseException("课程不存在");
        }

        // 获取课程教师ID
        Long teacherId = course.getTeacherId();
        if (teacherId == null) {
            throw new BaseException("课程已不存在");
        }""")

patch(r'selection-score-service\src\main\java\com\smartteaching\service\score\ScoreServiceImpl.java',
"""import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.ScoreMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.mapper.ScoreMapper;""")

patch(r'selection-score-service\src\main\java\com\smartteaching\service\score\ScoreServiceImpl.java',
"""    private CourseMapper courseMapper;""",
"""    private CourseFeignClient courseFeignClient;""")

patch(r'selection-score-service\src\main\java\com\smartteaching\service\score\ScoreServiceImpl.java',
"""        // 获取教师ID
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new RuntimeException("课程不存在或已停用");
        }""",
"""        // 获取教师ID（跨服务 Feign 调用 course-service）
        CourseInfoVO course = courseFeignClient.getCourseById(courseId).getData();
        if (course == null || course.getStatus() != 1) {
            throw new RuntimeException("课程不存在或已停用");
        }""")

# ==================== 5. teaching-service：Qa 改造 ====================
patch(r'teaching-service\src\main\java\com\smartteaching\service\qa\QaServiceImpl.java',
"""import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.QaMapper;
import com.smartteaching.mapper.QaReplyMapper;
import com.smartteaching.mapper.SelectionMapper;
import com.smartteaching.mapper.UserMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.client.SelectionFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.mapper.QaMapper;
import com.smartteaching.mapper.QaReplyMapper;
import com.smartteaching.mapper.UserMapper;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\qa\QaServiceImpl.java',
"""    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SelectionMapper selectionMapper;""",
"""    @Resource
    private CourseFeignClient courseFeignClient;

    @Resource
    private SelectionFeignClient selectionFeignClient;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\qa\QaServiceImpl.java',
"""        // 校验课程存在且启用
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BaseException("所选课程不存在");
        }""",
"""        // 校验课程存在且启用（跨服务 Feign 调用 course-service）
        CourseInfoVO course = courseFeignClient.getCourseById(dto.getCourseId()).getData();
        if (course == null) {
            throw new BaseException("所选课程不存在");
        }""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\qa\QaServiceImpl.java',
"""            boolean selected = selectionMapper.hasSelected(currentUser.getId(), course.getId());""",
"""            boolean selected = Boolean.TRUE.equals(
                    selectionFeignClient.hasSelected(currentUser.getId(), course.getId()).getData());""")

# ==================== 6. teaching-service：Evaluation 改造 ====================
patch(r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
"""import com.smartteaching.mapper.CourseMapper;
import com.smartteaching.mapper.EvaluationMapper;
import com.smartteaching.mapper.UserMapper;""",
"""import com.smartteaching.api.client.CourseFeignClient;
import com.smartteaching.api.client.UserFeignClient;
import com.smartteaching.api.dto.CourseInfoVO;
import com.smartteaching.api.dto.UserInfoVO;
import com.smartteaching.mapper.EvaluationMapper;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
"""    private CourseMapper courseMapper;
    @Resource
    private UserMapper userMapper;""",
"""    private CourseFeignClient courseFeignClient;
    @Resource
    private UserFeignClient userFeignClient;""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
"""        //校验课程
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BaseException("课程不存在");
        }""",
"""        //校验课程（跨服务 Feign 调用 course-service）
        CourseInfoVO course = courseFeignClient.getCourseById(dto.getCourseId()).getData();
        if (course == null) {
            throw new BaseException("课程不存在");
        }""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
"""            // 学生姓名（脱敏）
            User student = userMapper.selectById(eval.getStudentId());
            if (student != null) {""",
"""            // 学生姓名（脱敏，跨服务 Feign 调用 uaa-service）
            UserInfoVO student = userFeignClient.getUserById(eval.getStudentId()).getData();
            if (student != null) {""")

patch(r'teaching-service\src\main\java\com\smartteaching\service\evaluation\EvaluationServiceImpl.java',
"""        // 1. 查询该教师的所有课程
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getTeacherId, teacherId)
                .eq(Course::getStatus, 1);
        List<Course> courses = courseMapper.selectList(courseWrapper);
        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());""",
"""        // 1. 查询该教师的所有课程（跨服务 Feign 调用 course-service）
        List<CourseInfoVO> courses = courseFeignClient.listCourses(1, teacherId).getData();
        if (courses == null) {
            courses = new ArrayList<>();
        }
        List<Long> courseIds = courses.stream().map(CourseInfoVO::getId).collect(Collectors.toList());""")

print('=' * 40)
print('patched: %d, failed: %d' % (ok_count, len(fail_list)))
for f in fail_list:
    print('FAIL', f)
