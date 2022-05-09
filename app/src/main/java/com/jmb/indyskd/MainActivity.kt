package com.jmb.indyskd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jmb.indyskd.databinding.ActivityMainBinding
import org.hyperledger.aries.api.LoggerProvider
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import org.hyperledger.aries.storage.Provider
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }
}