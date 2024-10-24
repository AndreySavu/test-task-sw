package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.path.normal.post


fun NormalOpenAPIRoute.author() {
    route("/author/add").post<Unit, AuthorRecord, AuthorRecord>(info("Добавить автора")) { param, body ->
        respond(AuthorService.addRecord(body))
    }
}

data class AuthorRecord(
    val fullName: String
)