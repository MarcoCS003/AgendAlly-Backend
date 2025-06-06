package com.example.routes

import com.example.*
import com.example.services.BlogEventsService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.blogEventsRoutes() {
    val blogEventsService = BlogEventsService()

    route("/api") {

        // ============== RUTAS DE EVENTOS ==============
        route("/events") {

            // GET /api/events - Obtener todos los eventos
            get {
                try {
                    val events = blogEventsService.getAllEvents()
                    val response = EventsListResponse(
                        events = events,
                        total = events.size
                    )
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener eventos: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // GET /api/events/search?q=query - Buscar eventos
            get("/search") {
                try {
                    val query = call.request.queryParameters["q"]
                    if (query.isNullOrBlank()) {
                        val errorResponse = ErrorResponse("Parámetro 'q' requerido para búsqueda")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@get
                    }

                    val events = blogEventsService.searchEvents(query)
                    val response = EventSearchResponse(
                        events = events,
                        total = events.size,
                        query = query
                    )
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error en búsqueda: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // GET /api/events/category/{category} - Eventos por categoría
            get("/category/{category}") {
                try {
                    val category = call.parameters["category"]
                    if (category.isNullOrBlank()) {
                        val errorResponse = ErrorResponse("Categoría requerida")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@get
                    }

                    val events = blogEventsService.getEventsByCategory(category.uppercase())
                    val response = EventsByCategoryResponse(
                        events = events,
                        total = events.size,
                        category = category
                    )
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener eventos por categoría: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // GET /api/events/upcoming - Eventos próximos
            get("/upcoming") {
                try {
                    val events = blogEventsService.getUpcomingEvents()
                    val response = UpcomingEventsResponse(
                        events = events,
                        total = events.size,
                        description = "Eventos próximos (siguientes 30 días)"
                    )
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener eventos próximos: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // GET /api/events/stats - Estadísticas de eventos
            get("/stats") {
                try {
                    val stats = blogEventsService.getEventStats()
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener estadísticas: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // GET /api/events/{id} - Obtener evento específico
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        val errorResponse = ErrorResponse("ID de evento inválido")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@get
                    }

                    val event = blogEventsService.getEventById(id)
                    if (event != null) {
                        call.respond(HttpStatusCode.OK, event)
                    } else {
                        val errorResponse = ErrorResponse("Evento no encontrado")
                        call.respond(HttpStatusCode.NotFound, errorResponse)
                    }
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener evento: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // POST /api/events - Crear nuevo evento
            post {
                try {
                    val request = call.receive<CreateEventRequest>()

                    // Validaciones básicas
                    if (request.title.isBlank()) {
                        val errorResponse = ErrorResponse("El título es requerido")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@post
                    }

                    val event = blogEventsService.createEvent(request)
                    call.respond(HttpStatusCode.Created, event)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al crear evento: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // PUT /api/events/{id} - Actualizar evento
            put("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        val errorResponse = ErrorResponse("ID de evento inválido")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@put
                    }

                    val request = call.receive<CreateEventRequest>()
                    val updatedEvent = blogEventsService.updateEvent(id, request)

                    if (updatedEvent != null) {
                        call.respond(HttpStatusCode.OK, updatedEvent)
                    } else {
                        val errorResponse = ErrorResponse("Evento no encontrado")
                        call.respond(HttpStatusCode.NotFound, errorResponse)
                    }
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al actualizar evento: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // DELETE /api/events/{id} - Eliminar evento
            delete("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        val errorResponse = ErrorResponse("ID de evento inválido")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@delete
                    }

                    val deleted = blogEventsService.deleteEvent(id)
                    if (deleted) {
                        val response = MessageResponse("Evento eliminado correctamente")
                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        val errorResponse = ErrorResponse("Evento no encontrado")
                        call.respond(HttpStatusCode.NotFound, errorResponse)
                    }
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al eliminar evento: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }
        }

        // ============== RUTAS DE INSTITUTOS CON EVENTOS ==============
        route("/institutes/{instituteId}/events") {

            // GET /api/institutes/{instituteId}/events - Eventos de un instituto específico
            get {
                try {
                    val instituteId = call.parameters["instituteId"]?.toIntOrNull()
                    if (instituteId == null) {
                        val errorResponse = ErrorResponse("ID de instituto inválido")
                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                        return@get
                    }

                    val response = blogEventsService.getEventsByInstitute(instituteId)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    val errorResponse = ErrorResponse("Error al obtener eventos del instituto: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }
        }
    }
}