package teammates.ui.webapi.action;

import java.time.ZoneId;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.FieldValidator;
import teammates.ui.webapi.output.CourseData;
import teammates.ui.webapi.request.CourseCreateRequest;

/**
 * Create a new course for an instructor.
 */
public class CreateCourseAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }
    }

    @Override
    public ActionResult execute() {
        CourseCreateRequest courseCreateRequest = getAndValidateRequestBody(CourseCreateRequest.class);

        String newCourseTimeZone = courseCreateRequest.getTimeZone();

        String timeZoneErrorMessage = FieldValidator.getInvalidityInfoForTimeZone(newCourseTimeZone);
        if (!timeZoneErrorMessage.isEmpty()) {
            return new JsonResult(timeZoneErrorMessage, HttpStatus.SC_BAD_REQUEST);
        }

        String newCourseId = courseCreateRequest.getCourseId();
        String newCourseName = courseCreateRequest.getCourseName();

        CourseAttributes courseAttributes =
                CourseAttributes.builder(newCourseId)
                        .withName(newCourseName)
                        .withTimezone(ZoneId.of(newCourseTimeZone))
                        .build();

        try {
            logic.createCourseAndInstructor(userInfo.getId(), courseAttributes);
        } catch (EntityAlreadyExistsException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_CONFLICT);
        } catch (InvalidParametersException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_BAD_REQUEST);
        }

        return new JsonResult(new CourseData(logic.getCourse(newCourseId)));
    }
}
