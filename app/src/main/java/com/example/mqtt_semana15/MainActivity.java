package com.example.mqtt_semana15;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
//Imports para MQTT
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {

    private static final String BROKER = "tcp://test.mosquitto.org";
    private static final String ID_Cliente = "MQTT_FX_Client";
    private MqttServ mqtt;
    private EditText etTopico;
    private EditText etMensaje;
    private TextView tvMensaje;
    private Button btnPublicar;
    private Button btnSuscribir;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mqtt = new MqttServ();
        //3 metodos principales de mqtt / ademas estos van con handler.post para que se ejecuten en el hilo principal de la aplicacion
        mqtt.setCallback(new MqttCallback() {
            @Override
            //Notifica si la conexion se perdio
            public void connectionLost(Throwable cause) {
                handler.post(() ->{
                    Toast.makeText(MainActivity.this,"Conexion perdida",Toast.LENGTH_SHORT).show();
                    mqtt.onConnectionLost(cause);
                });
            }

            @Override
            //Recibe el mensaje para mostrarlo por el textview
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                handler.post(() ->{
                   String mensajeRecibido = "Mensaje recibido en el topico"+ topic + ":" + new String(message.getPayload());
                   tvMensaje.append(mensajeRecibido + "\n");
                });
            }

            @Override
            //Notifica cuando el mensaje se mande correctamente
            public void deliveryComplete(IMqttDeliveryToken token) {
                handler.post(()->{
                    Toast.makeText(MainActivity.this, "Entrega completa", Toast.LENGTH_SHORT).show();
                });
            }
        });

        //MQTT
        etTopico = findViewById(R.id.etTopico);
        etMensaje = findViewById(R.id.etMensaje);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnPublicar = findViewById(R.id.btnPublicar);
        btnSuscribir = findViewById(R.id.btnSuscribir);

        handler = new Handler(Looper.getMainLooper());

        //Lo primero que se hace es conectar el mqtt con el broker entregandole la url y la id cliente
        try {
            mqtt.connect(BROKER,ID_Cliente);
        }catch (IllegalArgumentException e){
            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Para enviar algo debemos entregarle el topico y el mensaje para luego publicarlo
        btnPublicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topico = etTopico.getText().toString();
                String mensaje = etMensaje.getText().toString();
                mqtt.publish(topico,mensaje);
            }
        });

        //De la misma forma para recibirlo.
        btnSuscribir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topico = etTopico.getText().toString();
                mqtt.subscribe(topico);
                Toast.makeText(MainActivity.this, "Suscrito a: " + topico, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Cuando se deje de ejecutar esta app
    @Override
    protected void onDestroy() {
        mqtt.disconnect();
        super.onDestroy();
    }
}