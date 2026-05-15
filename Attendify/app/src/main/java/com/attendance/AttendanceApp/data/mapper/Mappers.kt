package com.attendance.attendanceapp.data.mapper

import com.attendance.attendanceapp.data.local.entity.*
import com.attendance.attendanceapp.data.remote.dto.*
import com.attendance.attendanceapp.domain.model.*

// --- User Mappers ---
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = try { Role.valueOf(role.lowercase()) } catch(e: Exception) { Role.student },
    status = try { UserStatus.valueOf(status.lowercase()) } catch(e: Exception) { UserStatus.pending }
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    password = "", // Managed via Auth
    role = role.name,
    status = status.name
)

fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = try { Role.valueOf(role.lowercase()) } catch(e: Exception) { Role.student },
    status = try { UserStatus.valueOf(status.lowercase()) } catch(e: Exception) { UserStatus.pending }
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    role = role.name,
    status = status.name
)

// --- Attendance Mappers ---
fun AttendanceEntity.toDomain(): Attendance = Attendance(
    id = id,
    studentId = studentId,
    sessionId = sessionId,
    timestamp = timestamp,
    status = try { AttendanceStatus.valueOf(status) } catch(e: Exception) { AttendanceStatus.Absent }
)

fun Attendance.toEntity(): AttendanceEntity = AttendanceEntity(
    id = id,
    studentId = studentId,
    sessionId = sessionId,
    status = status.name,
    timestamp = timestamp
)

fun AttendanceDto.toDomain(): Attendance = Attendance(
    id = id,
    studentId = studentId,
    sessionId = sessionId,
    status = try { AttendanceStatus.valueOf(status) } catch(e: Exception) { AttendanceStatus.Absent },
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
    isActive = isActive,
    durationMinutes = durationMinutes,
    maxStudents = maxStudents
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    sessionId = id,
    scheduleId = scheduleId,
    qrCode = qrCode,
    date = date,
    isActive = isActive,
    durationMinutes = durationMinutes,
    maxStudents = maxStudents
)

fun SessionDto.toDomain(): Session = Session(
    id = sessionId,
    scheduleId = scheduleId,
    qrCode = qrCode,
    date = date,
    isActive = isActive,
    durationMinutes = durationMinutes,
    maxStudents = maxStudents
)

fun Session.toDto(): SessionDto = SessionDto(
    sessionId = id,
    scheduleId = scheduleId,
    qrCode = qrCode,
    date = date,
    isActive = isActive,
    durationMinutes = durationMinutes,
    maxStudents = maxStudents
)

// --- Course Mappers ---
fun CourseEntity.toDomain(): Course = Course(
    id = courseId,
    name = name,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    courseId = id,
    name = name,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun CourseDto.toDomain(): Course = Course(
    id = id,
    name = name,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun Course.toDto(): CourseDto = CourseDto(
    id = id,
    name = name,
    departmentId = departmentId,
    year = year,
    semester = semester
)

// --- Department Mappers ---
fun DepartmentEntity.toDomain(): Department = Department(
    id = id,
    name = name
)

fun Department.toEntity(): DepartmentEntity = DepartmentEntity(
    id = id,
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
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun Student.toEntity(): StudentEntity = StudentEntity(
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun StudentDto.toDomain(): Student = Student(
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year,
    semester = semester
)

fun Student.toDto(): StudentDto = StudentDto(
    studentId = studentId,
    userId = userId,
    departmentId = departmentId,
    year = year,
    semester = semester
)

// --- Teacher Mappers ---
fun TeacherEntity.toDomain(): Teacher = Teacher(
    teacherId = teacherId.toString(),
    userId = userId.toString(),
    departmentId = departmentId?.toString(),
    specialization = specialization
)

fun Teacher.toEntity(): TeacherEntity = TeacherEntity(
    teacherId = teacherId,
    userId = userId,
    departmentId = departmentId,
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
    scheduleId = scheduleId,
    courseId = courseId,
    teacherId = teacherId,
    departmentId = departmentId,
    year = year,
    semester = semester,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
    scheduleId = scheduleId,
    courseId = courseId,
    teacherId = teacherId,
    departmentId = departmentId,
    year = year,
    semester = semester,
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
    semester = semester,
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
    semester = semester,
    dayOfWeek = dayOfWeek,
    startTime = startTime,
    endTime = endTime
)

// --- Notification Mappers ---

fun NotificationEntity.toDomain(): Notification = Notification(
    id = id.toString(),
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun NotificationDto.toDomain(): Notification = Notification(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)

fun Notification.toDto(): NotificationDto = NotificationDto(
    id = id,
    userId = userId,
    title = title,
    message = message,
    type = type,
    isRead = isRead,
    createdAt = createdAt
)
// --- Section Mappers ---
fun SectionEntity.toDomain(): Section = Section(
    id = sectionId,
    courseId = courseId,
    name = name,
    year = year,
    semester = semester
)

fun Section.toEntity(): SectionEntity = SectionEntity(
    sectionId = id,
    courseId = courseId,
    name = name,
    year = year,
    semester = semester
)

fun SectionDto.toDomain(): Section = Section(
    id = sectionId,
    courseId = courseId,
    name = name,
    year = year,
    semester = semester
)

fun Section.toDto(): SectionDto = SectionDto(
    sectionId = id,
    courseId = courseId,
    name = name,
    year = year,
    semester = semester
)
