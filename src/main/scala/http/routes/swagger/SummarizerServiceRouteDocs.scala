package http.routes.swagger

import akka.http.scaladsl.server._
import io.swagger.annotations._
import javax.ws.rs.Path
import models.{QueryResult, Report}


@Api(value = "/", produces = "application/json", consumes = "application/json")
@Path("/")
trait SummarizerServiceRouteDocs {

  @Path("/report")
  @ApiOperation(httpMethod = "GET", notes = "", value = "Retrieves a report.")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Retrieves a report.", response = classOf[Report]),
    new ApiResponse(code = 500, message = "Internal server error.")
  ))
  def getReport: Route


  @Path("/query/{countryReference}")
  @ApiOperation(httpMethod = "GET", notes = "", value = "Retrieves a query result.")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "countryReference", dataType = "string", paramType = "path", value = "Name or ISO code of the country to be queried.", required = true),
    new ApiImplicitParam(name = "referenceIsCode", dataType = "boolean", paramType = "query", value = "Flag that indicates if countryReference should be taken as a ISO code (true) or as a name (false).", defaultValue = "true", required = false)
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Retrieves a query result.", response = classOf[QueryResult]),
    new ApiResponse(code = 404, message = "Country not found.", response = classOf[String]),
    new ApiResponse(code = 500, message = "Internal server error.")
  ))
  def performQuery: Route
}
