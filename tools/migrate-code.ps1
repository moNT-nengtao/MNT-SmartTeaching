$ErrorActionPreference = "Stop"
$src = "C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-backend\src\main"
$root = "C:\Users\猫猫君\Desktop\MNT-SmartTeaching\mnt-st\smart-teaching-cloud"

# 服务 -> 需要从单体拷贝的目录清单（相对 src/main/java/com/smartteaching）
$map = @{
    "uaa-service" = @(
        "controller\auth", "controller\user", "controller\org",
        "service\auth", "service\user", "service\org",
        "entity\user", "entity\org",
        "common\dto\user", "common\dto\org",
        "common\vo\user", "common\vo\org"
    )
    "course-service" = @(
        "controller\course", "controller\schedule",
        "service\course", "service\service",
        "entity\course",
        "common\dto\course", "common\dto\schedule",
        "common\vo\course", "common\vo\schedule"
    )
    "selection-score-service" = @(
        "controller\selection", "controller\score",
        "service\selection", "service\score",
        "entity\selection", "entity\score",
        "common\dto\selection", "common\dto\score",
        "common\vo\selection", "common\vo\score"
    )
    "attendance-service" = @(
        "controller\attendance",
        "service\attendance",
        "entity\attendance",
        "common\dto\attendance", "common\vo\attendance"
    )
    "warning-service" = @(
        "controller\warning",
        "service\warning",
        "entity\warning",
        "common\dto\warning", "common\vo\warning"
    )
    "ai-service" = @(
        "controller\ai",
        "service\ai",
        "entity\ai",
        "common\dto\ai", "common\vo\ai"
    )
    "analysis-service" = @(
        "controller\dashboard",
        "service\dashboard",
        "entity\dashboard",
        "common\vo\dashborad"
    )
    "teaching-service" = @(
        "controller\notice", "controller\qa", "controller\evaluation", "controller\homework",
        "service\notice", "service\qa", "service\evaluation", "service\homework",
        "entity\notice", "entity\qa", "entity\evaluation", "entity\homework",
        "common\dto\notice", "common\dto\qa", "common\dto\evaluation", "common\dto\homework",
        "common\vo\notice", "common\vo\qa", "common\vo\evaluation", "common\vo\homework"
    )
}

# mapper 归属：服务 -> Mapper 接口文件列表
$mapperMap = @{
    "uaa-service" = @("UserMapper.java", "OrgMapper.java")
    "course-service" = @("CourseMapper.java", "ScheduleMapper.java")
    "selection-score-service" = @("SelectionMapper.java", "ScoreMapper.java")
    "attendance-service" = @("AttendanceMapper.java", "AttendanceRecordMapper.java")
    "warning-service" = @("WarningMapper.java")
    "ai-service" = @("AiSessionMapper.java", "AiMessageMapper.java", "AiDailyUsageMapper.java", "AiAnalysisMapper.java")
    "analysis-service" = @("DashboardMapper.java")
    "teaching-service" = @("NoticeMapper.java", "NoticeReadRecordMapper.java", "QaMapper.java", "QaReplyMapper.java", "EvaluationMapper.java", "HomeworkMapper.java", "HomeworkSubmissionMapper.java")
}

# XML 归属：服务 -> XML 文件列表
$xmlMap = @{
    "uaa-service" = @("UserMapper.xml", "OrgMapper.xml")
    "course-service" = @("CourseMapper.xml", "ScheduleMapper.xml")
    "selection-score-service" = @("SelectionMapper.xml", "ScoreMapper.xml")
    "attendance-service" = @("AttendanceMapper.xml", "AttendanceRecordMapper.xml")
    "warning-service" = @("WarningMapper.xml")
    "ai-service" = @("AiMapper.xml")
    "analysis-service" = @("DashboardMapper.xml")
    "teaching-service" = @("NoticeMapper.xml", "QaMapper.xml", "EvaluationMapper.xml", "HomeworkMapper.xml")
}

$javaBase = "$src\java\com\smartteaching"
$mapperDir = "$javaBase\mapper"
$xmlSrcDir = "$src\resources\mapper"

foreach ($svc in $map.Keys) {
    $dstJava = "$root\$svc\src\main\java\com\smartteaching"
    $copied = 0

    foreach ($rel in $map[$svc]) {
        $from = "$javaBase\$rel"
        if (Test-Path $from) {
            $to = "$dstJava\$rel"
            New-Item -ItemType Directory -Path $to -Force | Out-Null
            Copy-Item "$from\*" $to -Recurse -Force
            $copied += (Get-ChildItem $from -File).Count
        } else {
            Write-Host "WARN missing: $rel"
        }
    }

    # mapper 接口（含跨服务通用的 UserMapper）
    $dstMapper = "$dstJava\mapper"
    New-Item -ItemType Directory -Path $dstMapper -Force | Out-Null
    if ($mapperMap.ContainsKey($svc)) {
        foreach ($m in $mapperMap[$svc]) {
            Copy-Item "$mapperDir\$m" "$dstMapper\" -Force
            $copied++
        }
    }
    # 每个业务服务都要 UserMapper（JWT 过滤器 + getCurrentUser + CustomUserDetailsService）
    if ($svc -ne "uaa-service" -and -not ($mapperMap[$svc] -contains "UserMapper.java")) {
        Copy-Item "$mapperDir\UserMapper.java" "$dstMapper\" -Force
        $copied++
    }
    # CustomUserDetailsService（每个服务安全链路必需）
    $dstUserSvc = "$dstJava\service\user"
    New-Item -ItemType Directory -Path $dstUserSvc -Force | Out-Null
    Copy-Item "$javaBase\service\user\CustomUserDetailsService.java" "$dstUserSvc\" -Force
    $copied++

    # mapper XML
    $dstRes = "$root\$svc\src\main\resources\mapper"
    New-Item -ItemType Directory -Path $dstRes -Force | Out-Null
    foreach ($x in $xmlMap[$svc]) {
        Copy-Item "$xmlSrcDir\$x" "$dstRes\" -Force
        $copied++
    }
    if ($svc -ne "uaa-service" -and -not ($xmlMap[$svc] -contains "UserMapper.xml")) {
        Copy-Item "$xmlSrcDir\UserMapper.xml" "$dstRes\" -Force
        $copied++
    }

    Write-Host "$svc : copied $copied files"
}
Write-Host "DONE"
