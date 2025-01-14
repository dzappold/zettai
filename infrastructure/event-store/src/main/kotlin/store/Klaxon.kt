package store

import ListName
import User
import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import commands.EntityId
import java.time.Instant
import java.time.LocalDate
import java.util.*

internal val klaxon = Klaxon()
    .converter(EntityIdConverter)
    .converter(UserConverter)
    .converter(ListNameConverter)
    .converter(LocalDateConverter)
    .converter(InstantConverter)

object EntityIdConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == EntityId::class.java

    override fun fromJson(jv: JsonValue): EntityId =
        EntityId(UUID.fromString(jv.string))

    override fun toJson(value: Any): String =
        """"${(value as EntityId).raw}""""
}

object UserConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == User::class.java

    override fun fromJson(jv: JsonValue): User? =
        jv.string?.let {
            User(it)
        }

    override fun toJson(value: Any): String =
        """"${(value as? User)?.name}""""
}

object ListNameConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean = cls == ListName::class.java

    override fun fromJson(jv: JsonValue): ListName? =
        jv.string?.let {
            ListName.fromUntrusted(it)
        }

    override fun toJson(value: Any): String =
        """"${(value as? ListName)?.name}""""
}

object LocalDateConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

    override fun fromJson(jv: JsonValue): LocalDate? =
        jv.string?.let {
            LocalDate.parse(it)
        }

    override fun toJson(value: Any) = """"${(value as? LocalDate)}""""
}

object InstantConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == Instant::class.java

    override fun fromJson(jv: JsonValue): Instant? =
        jv.string?.let {
            Instant.parse(it)
        }

    override fun toJson(value: Any) = """"${(value as? Instant)}""""
}
