package com.example.database

import com.example.Career
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime

// Tabla de Institutos
object Institutes : Table("institutes") {
    val id = integer("id").autoIncrement()
    val acronym = varchar("acronym", 10)
    val name = varchar("name", 255)
    val address = text("address")
    val email = varchar("email", 100)
    val phone = varchar("phone", 20)
    val studentNumber = integer("student_number")
    val teacherNumber = integer("teacher_number")
    val webSite = varchar("website", 255).nullable()
    val facebook = varchar("facebook", 255).nullable()
    val instagram = varchar("instagram", 255).nullable()
    val twitter = varchar("twitter", 255).nullable()
    val youtube = varchar("youtube", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

// Tabla de Carreras
object Careers : Table("careers") {
    val id = integer("id").autoIncrement()
    val careerID = integer("career_id")
    val name = varchar("name", 255)
    val acronym = varchar("acronym", 50)
    val email = varchar("email", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val instituteId = reference("institute_id", Institutes.id)

    override val primaryKey = PrimaryKey(id)
}

// NUEVA: Tabla de Eventos del Blog
object BlogEvents : Table("blog_events") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val shortDescription = text("short_description")
    val longDescription = text("long_description")
    val location = varchar("location", 255)
    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()
    val category = varchar("category", 50).default("INSTITUTIONAL") // INSTITUTIONAL, CAREER, PERSONAL
    val imagePath = varchar("image_path", 500)
    val instituteId = reference("institute_id", Institutes.id)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

// Configuración de la base de datos
fun initDatabase() {
    // Conectar a H2 (base de datos en memoria para desarrollo)
    Database.connect(
        url = "jdbc:h2:mem:academically;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver"
    )

    // Crear tablas
    transaction {
        SchemaUtils.create(Institutes, Careers, BlogEvents) // Agregamos BlogEvents

        // Insertar datos de ejemplo
        insertSampleData()
        insertSampleEvents() // Nueva función para eventos
    }

    println("✅ Base de datos H2 inicializada")
    println("📊 URL: jdbc:h2:mem:academically")
    println("🔗 Console: http://localhost:8080/h2-console")
}

// Insertar datos de muestra (tus institutos actuales)
fun insertSampleData() {
    // Solo insertar si no hay datos
    if (Institutes.selectAll().count() > 0) return

    // Instituto Tecnológico de Puebla
    val itpId = Institutes.insert {
        it[acronym] = "ITP"
        it[name] = "Instituto Tecnológico de Puebla"
        it[address] = "Del Tecnológico 420, Corredor Industrial la Ciénega, 72220 Heroica Puebla de Zaragoza, Pue."
        it[email] = "info@puebla.tecnm.mx"
        it[phone] = "222 229 8810"
        it[studentNumber] = 6284
        it[teacherNumber] = 298
        it[webSite] = "https://www.puebla.tecnm.mx"
        it[facebook] = "https://www.facebook.com/TecNMPuebla"
        it[instagram] = "https://www.instagram.com/tecnmpuebla"
        it[youtube] = "https://www.youtube.com/user/TECPUEBLA"
    } get Institutes.id

    // Carreras del ITP
    val itpCareers = listOf(
        Career(1, "Ingeniería en Tecnologías de la Información y Comunicaciones", "TICS"),
        Career(2, "Ingeniería Industrial", "Ing. Indust"),
        Career(3, "Ingeniería Electrónica", "Electrónica"),
        Career(4, "Ingeniería Eléctrica", "Eléctrica"),
        Career(5, "Ingeniería en Gestión Empresarial", "Gestión Empresarial"),
        Career(6, "Ingeniería Mecánica", "Mecánica")
    )

    itpCareers.forEach { career ->
        Careers.insert {
            it[careerID] = career.careerID
            it[name] = career.name
            it[acronym] = career.acronym
            it[email] = career.email
            it[phone] = career.phone
            it[instituteId] = itpId
        }
    }

    // Instituto Tecnológico de Tijuana
    val ittId = Institutes.insert {
        it[acronym] = "ITT"
        it[name] = "Instituto Tecnológico de Tijuana"
        it[address] = "Calzada del Tecnológico S/N, Fraccionamiento Tomas Aquino, 22414 Tijuana, B.C."
        it[email] = "webmaster@tectijuana.mx"
        it[phone] = "664 607 8400"
        it[studentNumber] = 7500
        it[teacherNumber] = 350
        it[webSite] = "https://www.tijuana.tecnm.mx"
        it[facebook] = "https://www.facebook.com/tectijuana"
        it[instagram] = "https://www.instagram.com/tecnmtijuana"
    } get Institutes.id

    // Carreras del ITT
    val ittCareers = listOf(
        Career(7, "Licenciatura en Administración", "Administración"),
        Career(8, "Ingeniería en Tecnologías de la Información y Comunicaciones", "TICS")
    )

    ittCareers.forEach { career ->
        Careers.insert {
            it[careerID] = career.careerID
            it[name] = career.name
            it[acronym] = career.acronym
            it[email] = career.email
            it[phone] = career.phone
            it[instituteId] = ittId
        }
    }

    // Agregar más institutos...
    addMoreInstitutes()

    println("✅ Datos de ejemplo insertados: ${Institutes.selectAll().count()} institutos")
}

// NUEVA: Insertar eventos de ejemplo
fun insertSampleEvents() {
    // Solo insertar si no hay eventos
    if (BlogEvents.selectAll().count() > 0) return

    // Obtener el ID del ITP para los eventos
    val itpId = Institutes.select { Institutes.acronym eq "ITP" }
        .single()[Institutes.id]

    // Eventos del Instituto Tecnológico de Puebla
    val sampleEvents = listOf(
        Triple(
            "INNOVATECNMN 2025",
            "Registro para estudiantes lider",
            "Cumbre nacional de desarrollo tecnológico, investigación e innovación INOVATECNM. Dirigida al estudiantado inscrito\n" +
                    " al periodo Enero-Junio 2025 personal docente y de investigación del Instituto Tecnológico de Puebla"
        ) to LocalDate.of(2025, 11, 28),

        Triple(
            "Congreso Internacional en agua limpia y saneamiento del TECNM",
            "Registro para estudiantes",
            "Participa en el 1er. Congreso Internacional de Agua Limpia y Saneamiento del TECNM"
        ) to LocalDate.of(2025, 9, 25),

        Triple(
            "Concurso de Programación 2025",
            "Para estudiantes de TICS",
            "Invitación a los estudiantes de TICS a participar en el concurso de programación de 2025 sin costo"
        ) to LocalDate.of(2025, 4, 28),

        Triple(
            "Jornadas de TICS 2025",
            "Conferencias internacionales",
            "Participa en las jornadas de TICS del año 2025 con conferencistas internacionales, estaremos enfocados en el auge de la inteligencia artificial"
        ) to LocalDate.of(2025, 9, 15),

        Triple(
            "Plática de Servicio Social",
            "Información importante",
            "Información sobre los requisitos y proceso para realizar el servicio social"
        ) to LocalDate.of(2025, 5, 10)
    )

    sampleEvents.forEachIndexed { index, (eventData, date) ->
        val (title, shortDesc, longDesc) = eventData
        BlogEvents.insert {
            it[this.title] = title
            it[shortDescription] = shortDesc
            it[longDescription] = longDesc
            it[location] = if (index < 2) "Edificio 53" else "Edificio 36"
            it[startDate] = date
            it[endDate] = date
            it[category] = if (index < 2) "INSTITUTIONAL" else "CAREER"
            it[imagePath] = ""
            it[instituteId] = itpId
            it[isActive] = true
        }
    }

    println("✅ Eventos de ejemplo insertados: ${BlogEvents.selectAll().count()} eventos")
}

fun addMoreInstitutes() {
    // Instituto Tecnológico de Hermosillo
    val ithId = Institutes.insert {
        it[acronym] = "ITH"
        it[name] = "Instituto Tecnológico de Hermosillo"
        it[address] = "Av. Tecnológico S/N, Col. El Sahuaro, 83170 Hermosillo, Son."
        it[email] = "contacto@hermosillo.tecnm.mx"
        it[phone] = "662 260 6500"
        it[studentNumber] = 4200
        it[teacherNumber] = 220
        it[webSite] = "https://www.hermosillo.tecnm.mx"
        it[facebook] = "https://www.facebook.com/TecNMHermosillo"
    } get Institutes.id

    listOf(
        Career(9, "Ingeniería Industrial", "Ing. Indust"),
        Career(10, "Ingeniería Electrónica", "Electrónica")
    ).forEach { career ->
        Careers.insert {
            it[careerID] = career.careerID
            it[name] = career.name
            it[acronym] = career.acronym
            it[instituteId] = ithId
        }
    }

    // Instituto Tecnológico de Toluca
    val ittolId = Institutes.insert {
        it[acronym] = "ITT"
        it[name] = "Instituto Tecnológico de Toluca"
        it[address] = "Av. Tecnológico s/n, Agrícola Bella Vista, 52149 Metepec, Méx."
        it[email] = "webmaster@toluca.tecnm.mx"
        it[phone] = "722 208 7200"
        it[studentNumber] = 5800
        it[teacherNumber] = 310
        it[webSite] = "https://www.toluca.tecnm.mx"
    } get Institutes.id

    listOf(
        Career(11, "Ingeniería Eléctrica", "Eléctrica"),
        Career(12, "Ingeniería en Gestión Empresarial", "Gestión Empresarial")
    ).forEach { career ->
        Careers.insert {
            it[careerID] = career.careerID
            it[name] = career.name
            it[acronym] = career.acronym
            it[instituteId] = ittolId
        }
    }

    // Instituto Tecnológico Superior de Xalapa
    val itsxId = Institutes.insert {
        it[acronym] = "ITSX"
        it[name] = "Instituto Tecnológico Superior de Xalapa"
        it[address] = "Sección 5A, Reserva Territorial, 91060 Xalapa, Ver."
        it[email] = "contacto@itsx.edu.mx"
        it[phone] = "228 165 0525"
        it[studentNumber] = 3800
        it[teacherNumber] = 190
        it[webSite] = "https://www.itsx.edu.mx"
        it[facebook] = "https://www.facebook.com/ITSXalapa"
        it[instagram] = "https://www.instagram.com/itsxalapa"
        it[youtube] = "https://www.youtube.com/user/ITSXalapa"
    } get Institutes.id

    listOf(
        Career(13, "Ingeniería Mecánica", "Mecánica"),
        Career(14, "Licenciatura en Administración", "Administración")
    ).forEach { career ->
        Careers.insert {
            it[careerID] = career.careerID
            it[name] = career.name
            it[acronym] = career.acronym
            it[instituteId] = itsxId
        }
    }
}