package com.example.services

import com.example.Career
import com.example.Institute
import com.example.InstituteSearchResponse
import com.example.database.Careers
import com.example.database.Institutes
import io.ktor.util.reflect.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InstituteService {

    fun getAllInstitutes(): List<Institute> = transaction {
        Institutes.selectAll().map { instituteRow ->
            val instituteId = instituteRow[Institutes.id]

            // Obtener carreras de este instituto
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

            // Crear objeto Institute
            Institute(
                instituteID = instituteRow[Institutes.id],
                acronym = instituteRow[Institutes.acronym],
                name = instituteRow[Institutes.name],
                address = instituteRow[Institutes.address],
                email = instituteRow[Institutes.email],
                phone = instituteRow[Institutes.phone],
                studentNumber = instituteRow[Institutes.studentNumber],
                teacherNumber = instituteRow[Institutes.teacherNumber],
                webSite = instituteRow[Institutes.webSite],
                facebook = instituteRow[Institutes.facebook],
                instagram = instituteRow[Institutes.instagram],
                twitter = instituteRow[Institutes.twitter],
                youtube = instituteRow[Institutes.youtube],
                listCareer = careers
            )
        }
    }


    fun searchInstitutes(query: String): InstituteSearchResponse = transaction {
        val searchQuery = "%${query.lowercase()}%"

        val institutes = Institutes.select {
            (Institutes.name.lowerCase() like searchQuery) or
                    (Institutes.acronym.lowerCase() like searchQuery)
        }.map { instituteRow ->
            val instituteId = instituteRow[Institutes.id]

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

            Institute(
                instituteID = instituteRow[Institutes.id],
                acronym = instituteRow[Institutes.acronym],
                name = instituteRow[Institutes.name],
                address = instituteRow[Institutes.address],
                email = instituteRow[Institutes.email],
                phone = instituteRow[Institutes.phone],
                studentNumber = instituteRow[Institutes.studentNumber],
                teacherNumber = instituteRow[Institutes.teacherNumber],
                webSite = instituteRow[Institutes.webSite],
                facebook = instituteRow[Institutes.facebook],
                instagram = instituteRow[Institutes.instagram],
                twitter = instituteRow[Institutes.twitter],
                youtube = instituteRow[Institutes.youtube],
                listCareer = careers
            )
        }

        InstituteSearchResponse(
            institutes = institutes,
            total = institutes.size
        )
    }

    /**
     * Obtener un instituto específico por ID
     */
    fun getInstituteById(id: Int): Institute? = transaction {
        Institutes.select { Institutes.id eq id }.singleOrNull()?.let { instituteRow ->
            val careers = Careers.select { Careers.instituteId eq id }
                .map { careerRow ->
                    Career(
                        careerID = careerRow[Careers.careerID],
                        name = careerRow[Careers.name],
                        acronym = careerRow[Careers.acronym],
                        email = careerRow[Careers.email],
                        phone = careerRow[Careers.phone]
                    )
                }

            Institute(
                instituteID = instituteRow[Institutes.id],
                acronym = instituteRow[Institutes.acronym],
                name = instituteRow[Institutes.name],
                address = instituteRow[Institutes.address],
                email = instituteRow[Institutes.email],
                phone = instituteRow[Institutes.phone],
                studentNumber = instituteRow[Institutes.studentNumber],
                teacherNumber = instituteRow[Institutes.teacherNumber],
                webSite = instituteRow[Institutes.webSite],
                facebook = instituteRow[Institutes.facebook],
                instagram = instituteRow[Institutes.instagram],
                twitter = instituteRow[Institutes.twitter],
                youtube = instituteRow[Institutes.youtube],
                listCareer = careers
            )
        }
    }

    fun getInstituteStats(): Map<String, Int> = transaction {
        val totalStudents = Institutes
            .slice(Institutes.studentNumber.sum())
            .selectAll()
            .map { it[Institutes.studentNumber.sum()] }
            .firstOrNull() ?: 0

        val totalTeachers = Institutes
            .slice(Institutes.teacherNumber.sum())
            .selectAll()
            .map { it[Institutes.teacherNumber.sum()] }
            .firstOrNull() ?: 0

        mapOf(
            "totalInstitutes" to Institutes.selectAll().count().toInt(),
            "totalCareers" to Careers.selectAll().count().toInt(),
            "totalStudents" to totalStudents,
            "totalTeachers" to totalTeachers
        )
    }
}