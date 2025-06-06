package com.example.services

import com.example.*
import com.example.database.BlogEvents
import com.example.database.Institutes
import com.example.database.Careers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BlogEventsService {

    /**
     * Obtener todos los eventos activos
     */
    fun getAllEvents(): List<EventInstituteBlog> = transaction {
        BlogEvents.selectAll()
            .orWhere { BlogEvents.isActive eq true }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { rowToEvent(it) }
    }

    /**
     * Obtener eventos de un instituto específico
     */
    fun getEventsByInstitute(instituteId: Int): BlogEventsResponse = transaction {
        // Obtener información del instituto
        val institute = Institutes.select { Institutes.id eq instituteId }
            .singleOrNull()?.let { rowToInstitute(it) }

        // Obtener eventos del instituto
        val events = BlogEvents.selectAll()
            .orWhere { (BlogEvents.instituteId eq instituteId) and (BlogEvents.isActive eq true) }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { rowToEvent(it) }

        BlogEventsResponse(
            events = events,
            total = events.size,
            instituteInfo = institute
        )
    }

    /**
     * Obtener evento por ID
     */
    fun getEventById(eventId: Int): EventInstituteBlog? = transaction {
        BlogEvents.select { BlogEvents.id eq eventId }
            .singleOrNull()?.let { rowToEvent(it) }
    }

    /**
     * Buscar eventos por título o descripción
     */
    fun searchEvents(query: String): List<EventInstituteBlog> = transaction {
        val searchTerm = "%$query%"

        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.isActive eq true) and
                        ((BlogEvents.title like searchTerm) or
                                (BlogEvents.shortDescription like searchTerm) or
                                (BlogEvents.longDescription like searchTerm))
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { rowToEvent(it) }
    }

    /**
     * Crear nuevo evento
     */
    fun createEvent(request: CreateEventRequest): EventInstituteBlog = transaction {
        val eventId = BlogEvents.insert {
            it[title] = request.title
            it[shortDescription] = request.shortDescription
            it[longDescription] = request.longDescription
            it[location] = request.location
            it[startDate] = DateUtils.parseDate(request.startDate)
            it[endDate] = DateUtils.parseDate(request.endDate)
            it[category] = request.category
            it[imagePath] = request.imagePath
            it[instituteId] = request.instituteId
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
            it[isActive] = true
        } get BlogEvents.id

        getEventById(eventId)!!
    }

    /**
     * Actualizar evento existente
     */
    fun updateEvent(eventId: Int, request: CreateEventRequest): EventInstituteBlog? = transaction {
        val updated = BlogEvents.update({ BlogEvents.id eq eventId }) {
            it[title] = request.title
            it[shortDescription] = request.shortDescription
            it[longDescription] = request.longDescription
            it[location] = request.location
            it[startDate] = DateUtils.parseDate(request.startDate)
            it[endDate] = DateUtils.parseDate(request.endDate)
            it[category] = request.category
            it[imagePath] = request.imagePath
            it[updatedAt] = LocalDateTime.now()
        }

        if (updated > 0) getEventById(eventId) else null
    }

    /**
     * Eliminar evento (marcarlo como inactivo)
     */
    fun deleteEvent(eventId: Int): Boolean = transaction {
        BlogEvents.update({ BlogEvents.id eq eventId }) {
            it[isActive] = false
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    /**
     * Obtener eventos por categoría
     */
    fun getEventsByCategory(category: String): List<EventInstituteBlog> = transaction {
        BlogEvents.selectAll()
            .orWhere { (BlogEvents.category eq category) and (BlogEvents.isActive eq true) }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { rowToEvent(it) }
    }

    /**
     * Obtener eventos próximos (siguientes 30 días)
     */
    fun getUpcomingEvents(): List<EventInstituteBlog> = transaction {
        val today = LocalDate.now()
        val futureDate = today.plusDays(30)

        BlogEvents.selectAll()
            .orWhere {
                (BlogEvents.isActive eq true) and
                        (BlogEvents.startDate greaterEq today) and
                        (BlogEvents.startDate lessEq futureDate)
            }
            .orderBy(BlogEvents.startDate to SortOrder.ASC)
            .map { rowToEvent(it) }
    }

    /**
     * Obtener estadísticas de eventos
     */
    fun getEventStats(): EventStatsResponse = transaction {
        val totalEvents = BlogEvents.selectAll().orWhere { BlogEvents.isActive eq true }.count()
        val eventsByCategory = BlogEvents
            .slice(BlogEvents.category, BlogEvents.id.count())
            .selectAll()
            .orWhere { BlogEvents.isActive eq true }
            .groupBy(BlogEvents.category)
            .associate {
                it[BlogEvents.category] to it[BlogEvents.id.count()]
            }

        EventStatsResponse(
            totalEvents = totalEvents,
            eventsByCategory = eventsByCategory,
            lastUpdated = LocalDateTime.now().toString()
        )
    }

    // =============== FUNCIONES AUXILIARES ===============

    private fun rowToEvent(row: ResultRow): EventInstituteBlog {
        return EventInstituteBlog(
            id = row[BlogEvents.id],
            title = row[BlogEvents.title],
            shortDescription = row[BlogEvents.shortDescription],
            longDescription = row[BlogEvents.longDescription],
            location = row[BlogEvents.location],
            startDate = DateUtils.formatDate(row[BlogEvents.startDate]),
            endDate = DateUtils.formatDate(row[BlogEvents.endDate]),
            category = row[BlogEvents.category],
            imagePath = row[BlogEvents.imagePath],
            instituteId = row[BlogEvents.instituteId],
            createdAt = row[BlogEvents.createdAt].toString(),
            updatedAt = row[BlogEvents.updatedAt].toString(),
            isActive = row[BlogEvents.isActive]
        )
    }

    private fun rowToInstitute(row: ResultRow): Institute {
        // Obtener carreras del instituto
        val instituteId = row[Institutes.id]
        val careers = Careers.select { Careers.instituteId eq instituteId }
            .map { careerRow ->
                Career(
                    careerID = careerRow[Careers.careerID],
                    name = careerRow[Careers.name],
                    acronym = careerRow[Careers.acronym],
                    email = careerRow[Careers.email],
                    phone = careerRow[Careers.phone]
                )
            }

        return Institute(
            instituteID = row[Institutes.id],
            acronym = row[Institutes.acronym],
            name = row[Institutes.name],
            address = row[Institutes.address],
            email = row[Institutes.email],
            phone = row[Institutes.phone],
            studentNumber = row[Institutes.studentNumber],
            teacherNumber = row[Institutes.teacherNumber],
            webSite = row[Institutes.webSite],
            facebook = row[Institutes.facebook],
            instagram = row[Institutes.instagram],
            twitter = row[Institutes.twitter],
            youtube = row[Institutes.youtube],
            listCareer = careers
        )
    }
}