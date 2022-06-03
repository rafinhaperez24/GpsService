package cr.ac.gpsservice.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (tableName = "location_table")
class Location(
    @PrimaryKey(autoGenerate = true)
                val locationId : Long?,
               val latitude : Double,
               val longitude : Double): Serializable {

}