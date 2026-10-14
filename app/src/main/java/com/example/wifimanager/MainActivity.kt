import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnConnect.setOnClickListener {
            val success = executeRootCommands(
                arrayOf(
                    "ip link set wlan1 up",
                    "wpa_supplicant -B -i wlan1 -c /data/local/tmp/wpa_supplicant.conf",
                    "udhcpc -i wlan1"
                )
            )
            if (success) {
                tvStatus.text = "Status: Connection sequence initiated."
            } else {
                tvStatus.text = "Status: Root permission denied or failed."
            }
        }

        btnDisconnect.setOnClickListener {
            executeRootCommands(
                arrayOf(
                    "killall wpa_supplicant",
                    "ip link set wlan1 down"
                )
            )
            tvStatus.text = "Status: Interface brought down."
        }
    }

    private fun executeRootCommands(commands: Array<String>): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)

            for (command in commands) {
                outputStream.writeBytes("$command\n")
                outputStream.flush()
            }
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            outputStream.close()

            process.waitFor() == 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
