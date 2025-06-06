package com.example.routes

import com.example.services.InstituteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.instituteRoutes() {
    val instituteService = InstituteService()

    route("/api/institutes") {

        // GET /api/institutes - Obtener todos los institutos
        get {
            try {
                val institutes = instituteService.getAllInstitutes()
                call.respond(HttpStatusCode.OK, institutes)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener institutos: ${e.message}")
                )
            }
        }

        // GET /api/institutes/search?q=query - Buscar institutos
        get("/search") {
            try {
                val query = call.request.queryParameters["q"]
                if (query.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Parámetro 'q' requerido para búsqueda")
                    )
                    return@get
                }

                val searchResult = instituteService.searchInstitutes(query)
                call.respond(HttpStatusCode.OK, searchResult)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error en búsqueda: ${e.message}")
                )
            }
        }

        // GET /api/institutes/{id} - Obtener instituto específico
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "ID de instituto inválido")
                    )
                    return@get
                }

                val institute = instituteService.getInstituteById(id)
                if (institute != null) {
                    call.respond(HttpStatusCode.OK, institute)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Instituto no encontrado")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener instituto: ${e.message}")
                )
            }
        }

        // GET /api/institutes/stats - Estadísticas
        get("/stats") {
            try {
                val stats = instituteService.getInstituteStats()
                call.respond(HttpStatusCode.OK, stats)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error al obtener estadísticas: ${e.message}")
                )
            }
        }
    }
}