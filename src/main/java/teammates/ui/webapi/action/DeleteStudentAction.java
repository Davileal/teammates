package teammates.ui.webapi.action;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;

/**
 * Action: deletes a student from a course.
 */
public class DeleteStudentAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (userInfo.isAdmin) {
            return;
        }

        if (userInfo.isInstructor) {
            String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
            InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, userInfo.id);
            gateKeeper.verifyAccessible(
                    instructor, logic.getCourse(courseId), Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);
            return;
        }
        throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
    }

    @Override
    public ActionResult execute() {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String studentId = getRequestParamValue(Const.ParamsNames.STUDENT_ID);

        String studentEmail = null;
        if (studentId == null) {
            studentEmail = getNonNullRequestParamValue(Const.ParamsNames.STUDENT_EMAIL);
        } else {
            StudentAttributes student = logic.getStudentForGoogleId(courseId, studentId);
            if (student != null) {
                studentEmail = student.getEmail();
            }
        }

        // if student is not found, fail silently
        if (studentEmail != null) {
            logic.deleteStudent(courseId, studentEmail);
        }

        return new JsonResult("Student is successfully deleted.", HttpStatus.SC_OK);
    }

}
