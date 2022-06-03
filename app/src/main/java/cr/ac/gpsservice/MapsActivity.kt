package cr.ac.gpsservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygon
import cr.ac.gpsservice.databinding.ActivityMapsBinding
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location
import cr.ac.gpsservice.service.GpsService
import org.json.JSONObject

private lateinit var mMap: GoogleMap
private lateinit var locationDatabase: LocationDatabase
private lateinit var layer : GeoJsonLayer

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationDatabase = LocationDatabase.getInstance(this)

        validaPermiso()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        iniciaServicio()
        definePoligono(googleMap)
        recuperarPuntos()

    }

    fun definePoligono(googleMap: GoogleMap){
        val geoJsonData= JSONObject("{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              -84.3187165260315,\n" +
                "              10.078410383915246\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.31215047836304,\n" +
                "              10.069621613995432\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.30333137512206,\n" +
                "              10.078220244793458\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.3187165260315,\n" +
                "              10.078410383915246\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}")

        layer = GeoJsonLayer(googleMap, geoJsonData)
        layer.addLayerToMap()

    }
    //----------------------------------No está en el punto---------------------------------------------
    /* //Inicio Función
    fun definePoligono(googleMap: GoogleMap){
        val geoJsonData= JSONObject("{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {},\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Polygon\",\n" +
                "        \"coordinates\": [\n" +
                "          [\n" +
                "            [\n" +
                "              -85.27587890625,\n" +
                "              10.757762756247049\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.220947265625,\n" +
                "              10.412183158667512\n" +
                "            ],\n" +
                "            [\n" +
                "              -84.957275390625,\n" +
                "              10.6822006000841\n" +
                "            ],\n" +
                "            [\n" +
                "              -85.27587890625,\n" +
                "              10.757762756247049\n" +
                "            ]\n" +
                "          ]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}")

        layer = GeoJsonLayer(googleMap, geoJsonData)
        layer.addLayerToMap()

    }*/ //Fin Función
     //------------------------------------No está en el punto--------------------------------------------------//


    /**
     * Obtener los puntos almacenados en la db y mostrarlos en el mapa
     */
    fun recuperarPuntos() {
        val ubicaciones: List<Location> = locationDatabase.locationDao.query()
        for (location in ubicaciones) {
            val sydney = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }

    /**
     * Hace un filtro del broadcast GPS(cr.ac.gpsservice.GPS_EVENT)
     * inicia el servicio (startService Gpsservice
     */
    fun iniciaServicio() {
        val filter = IntentFilter()
        filter.addAction(GpsService.GPS)
        val progreso = ProgressReciever()
        registerReceiver(progreso, filter)
        startService(Intent(this, GpsService::class.java))
    }

    fun validaPermiso() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                SOLICITAR_GPS
            )

        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SOLICITAR_GPS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // El usuario no aceptó los permisos
                    System.exit(1)
                }

            }
        }
    }

    //Recibe los mensajes broadcast del gps
    class ProgressReciever : BroadcastReceiver() {

        fun getPolygon(layer: GeoJsonLayer): GeoJsonPolygon? {
            for (feature in layer.features) {
                return feature.geometry as GeoJsonPolygon
            }
            return null
        }


        //se obtiene el parametro enviado por el servicio(location)
        //coloca en el mapa la localizacion
        //Mueve la camara a esa localizacion
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == GpsService.GPS) {
                val localizacion: Location = intent.getSerializableExtra("localizacion") as Location
                val punto = LatLng(localizacion.latitude, localizacion.longitude)
                mMap.addMarker(MarkerOptions().position(punto).title("Marker in punto"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(punto))

                if (PolyUtil.containsLocation(localizacion.latitude, localizacion.longitude, getPolygon(layer)!!.outerBoundaryCoordinates, false)){
                    Toast.makeText(context,"Está en el Punto ",Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(context,"NO está en el punto ",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
