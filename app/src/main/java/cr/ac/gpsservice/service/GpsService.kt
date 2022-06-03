package cr.ac.gpsservice.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper
import com.google.android.gms.location.*
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location


class GpsService : IntentService("GpsService") {

    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationDatabase: LocationDatabase

    companion object {
        val GPS = "cr.ac.gpsservice.GPS"
    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)
        getLocation()
    }

    /**
     * Inicializa los atributos
     * coloca un intervalo de actualizacion de 1000 y una prioridad de PRIORITY_HIGH_ACCURACY
     * Recibe la ubicacion de gps mediante un onLocationResult
     * envia un brodcast con una instancia  de location y la accion gps(cr.ac.service.GPS_EVENT
     * ademas guarda la localizacion en la db
     */
    @SuppressLint("MissingPermission")
    fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                if(locationRequest == null){
                    return
                }
                for (location in locationResult.locations){
                    val localizacion = Location(null, location.latitude, location.longitude)
                    val bcIntent = Intent()
                    bcIntent.action = GpsService.GPS
                    bcIntent.putExtra("localizacion", localizacion)
                    sendBroadcast(bcIntent)

                    locationDatabase.locationDao.insert(Location(null,localizacion.latitude,localizacion.longitude))

                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Looper.loop()
    }

}
