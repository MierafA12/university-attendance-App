package com.attendance.attendanceapp.data.mapper

import com.attendance.attendanceapp.data.local.entity.*
import com.attendance.attendanceapp.data.remote.dto.*
import com.attendance.attendanceapp.domain.model.*

// --- User Mappers ---
fun UserEntity.toDomain(): User = User(
    id = id.toString(),
    name = name,
    email = email,
    role = Role.valueOf(role)
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id.toIntOrNull() ?: 0,
    name = name,
    email = email,
    password = "", // Managed via Auth
    role = role.name
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = if (role.isNotEmpty()) Role.valueOf(role) else Role.student
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    role = role.name
)

// --- Attendance Mappers ---
fun AttendanceEntity.toDomain(): Attendance = Attendance(
    id = id.toString(),
    studentId = studentId.toString(),
    sessionId = sessionId.toString(),
    timestamp = timestamp,
    status = AttendanceStatus.valueOf(status)
)

fun Attendance.toEntity(): AttendanceEntity = AttendanceEntity(
    id = id.toIntOrNull() ?: 0,
    studentId = studentId.toIntOrNull() ?: 0,
    sessionId = sessionId.toIntOrNull() ?: 0,
    status = status.name,
    timestamp = timestamp
)

fun AttendanceDto.toDomain(): Attendance = Attendance(
    id = id,
    studentId = studentId,
    sessionId = sessionId,
    status = if (status.isNotEmpty()) AttendanceStatus.valueOf(status) else AttendanceStatus.Absent,
    timestamp = timestamp
)

fun Attendance.toDto(): AttendanceDto = AttendanceDto(
    id = id,
    studentId = studentId,
    sessionId = sessionId,
    status = status.name,
    timestamp = timestamp
)

// --- Session Mappers ---
fun SessionEntity.toDomain(): Session = Session(
    id = sessionId.toString(),
    scheduleId = scheduleId.toString(),
    qrCode = qrCode,
    date = date,
    isActive = isActive
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    sessionId = id.toIntOrNull() ?: 0,
    scheduleId = scheduleId.toIntOrNull() ?: 0,
    qrCode = qrCode,
    date = date,
    isActive = isActive
)

fun SessionDto.toDomain(): Session = Session(
    id = sessionId,
    scheduleId = scheduleId,
    qrCode = qrCode,
    date = date,
    isActive = isActive
)

fun Session.toDto(): SessionDto = SessionDto(
    sessionId = id,
    scheduleId = scheduleId,
    qrCode = qrCode,
    date = date,
    isActive = isActive
)

// --- Course Mappers ---
fun CourseEntity.toDomain(): Course = Course(
    id = courseId.toString(),
    name = name,
    departmentId = departmentId.toString(),
    year = year
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    courseId = id.toIntOrNull() ?: 0,
    name = name,
    departmentId = departmentId.toIntOrNull() ?: 0,
    year = year
)

fun CourseDto.toDomain(): Course = Course(
    id = id,
    name = name,
    departmentId = departmentId,
    year = year
)

fun Course.toDto(): CourseDto = CourseDto(
    id = id,
    name = name,
    departmentId = departmentId,
    year = year
)

// --- Department Mappers ---
fun DepartmentEntity.toDomain(): Department = Department(
    id = id.toString(),
    name = name
)

fun Department.toEntity(): DepartmentEntity = DepartmentEntity(
    id = id.toIntOrNull() ?: 0,
    name = name
)

fun DepartmentDto.toDomain(): Department = Department(
    id = id,
    name = name
)

fun Department.toDto(): DepartmentDto = DepartmentDto(
    id = id,
    name = name
)

// --- Student Mappers ---
fun StudentEntity.toDomain(): Student = Student(
    studentId = studentId.toString(),
    userId = userId.toString(),
    departmentId = departmentId?.toString(),
    year = year
)

fun Student.toEntity(): StudentEntity = StudentEntity(
    studentId = studentId.toIntOrNull() ?: 0,
    userId = userId.toIntOrNull() ?: 0,
    departmentId = departmentId?.toIntOrNull(),
    year = year
)

fun StudentDto.toDomain(): Student = Student(
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year
)

fun Student.toDto(): StudentDto = StudentDto(
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year
)

// --- Teacher Mappers ---
fun TeacherEntity.toDomain(): Teacher = Teacher(
    teacherId = teacherId.toString(),
    userId = userId.toString(),
    departmentId = departmentId?.toString(),
    specialization = specialization
)

fun Teacher.toEntity(): TeacherEntity = TeacherEntity(
    teacherId = teacherId.toIntOrNull() ?: 0,
    userId = userId.toIntOrNull() ?: 0,
    departmentId = departmentId?.toIntOrNull(),
    specialization = specialization
)

fun TeacherDto.toDomain(): Teacher = Teacher(
    teacherId = teacherId,
    userId = userId,
    departmentId = departmentId,
    specialization = specialization
)

fun Teacher.toDto(): TeacherDto = TeacherDto(
    teacherId = teacherId,
    userId = userId,
    departmentId = departmentId,
    specialization = specialization
)

// --- Schedule Mappers ---
fun ScheduleEntity.toDomain(): Schedule = Schedule(
    scheduleId = scheduleId.toString(),
    courseId = courseId.toString(),
    teacherId = teacherId.toString(),
    departmentId = departmentId.toString(),
    year = year,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
    scheduleId = scheduleId.toIntOrNull() ?: 0,
    courseId = courseId.toIntOrNull() ?: 0,
    teacherId = teacherId.toIntOrNull() ?: 0,
    departmentId = departmentId.toIntOrNull() ?: 0,
    year = year,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun ScheduleDto.toDomain(): Schedule = Schedule(
    scheduleId = scheduleId,
    courseId = courseId,
    teacherId = teacherId,
    departmentId = departmentId,
    year = year,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun Schedule.toDto(): ScheduleDto = ScheduleDto(
    scheduleId = scheduleId,
    courseId = courseId,
    teacherId = teacherId,
    departmentId = departmentId,
    year = year,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

// --- Notification Mappers ---
fun NotificationEntity.toDomain(): Notification = Notification(
    id = id.toString(),
    studentId = studentId.toString(),
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.toEntity(): NotificationEntity = NotificationEntity(
    id = id.toIntOrNull() ?: 0,
    studentId = studentId.toIntOrNull() ?: 0,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun NotificationDto.toDomain(): Notification = Notification(
    id = id,
    studentId = studentId,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.toDto(): NotificationDto = NotificationDto(
    id = id,
    studentId = studentId,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)
// --- Section Mappers ---
fun SectionEntity.toDomain(): Section = Section(
    id = sectionId.toString(),
    courseId = courseId.toString(),
    name = name,
    year = year
)

fun Section.toEntity(): SectionEntity = SectionEntity(
    sectionId = id.toIntOrNull() ?: 0,
    courseId = courseId.toIntOrNull() ?: 0,
    name = name,
    year = year
)

fun SectionDto.toDomain(): Section = Section(
    id = sectionId,
    courseId = courseId,
    name = name,
    year = year
)

fun Section.toDto(): SectionDto = SectionDto(
    sectionId = id,
    courseId = courseId,
    name = name,
    year = year
)
