package com.jmb.indyskd


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.jmb.indyskd.databinding.FragmentFirstBinding
import com.jmb.indyskd.model.InvitationOutBand
import com.jmb.indyskd.model.ResponseReceiveInvitation
import com.jmb.indyskd.utils.Service
import kotlinx.coroutines.*
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.nio.charset.StandardCharsets


class FirstFragment : Fragment() {


    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var agent: AriesController
    private lateinit var handler: MyHandler
    private lateinit var retrofit: Retrofit
    private lateinit var call: Call<String>
    private val agentURL = "192.168.0.12"
    private val mediatorURL = "http://192.168.0.12:8051"
    private var gson = Gson()

    //===============
    private var invitation = ""
    private var connectionIdRegisterMediator = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        retrofit = Retrofit.Builder()
            .baseUrl(mediatorURL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        val service: Service = retrofit.create(Service::class.java)
        call = service.invitation
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            createAgent()
        }


        binding.btnAcceptInvitationOutBound.setOnClickListener {
            acceptInvitationOutBound()
        }


        binding.btnGetStatus.setOnClickListener {
            getStatus()
        }

        binding.btnAcceptInvitationOutBoundAgent.setOnClickListener {
            acceptInvitationOutBound()
        }

        binding.btnRegisterMediator.setOnClickListener {
            registerMediator()
        }
        binding.btnAcceptInvitationOutBoundAgent.setOnClickListener {
            acceptInvitationOutBoundAgent()
        }


    }


    private suspend fun createAgent() = withContext(Dispatchers.IO) {
        val opts = Options()
        opts.label = "EPM Agent"
        opts.autoAccept = true
        opts.addOutboundTransport("ws")
        opts.transportReturnRoute = "all"
        opts.useLocalAgent = true
        opts.logLevel = "DEBUG"

        // create an aries agent instance
        try {
            agent = Ariesagent.new_(opts)
            handler = MyHandler()
            val registrationID: String = agent.registerHandler(handler, "didexchange_states")
            val reqData =
                "{\t\t\n\"@id\":\"$registrationID\",\n\t\t\"label\":\"EPM AGENT\",\n\t\t\"@type\":\"https://didcomm.org/didexchange/1.0/invitation\"\n}"
            Log.i("reqData", reqData)
            Log.v("RegistratioonId", registrationID)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "EPM Agent" + " created", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "EPM Agent" + " was not created", Toast.LENGTH_LONG).show()
            }
            Log.e("CreateAgent", e.message.toString())
        }
    }





    /**
     * Conexion con el mediador con outbound
     *
     */

    private fun acceptInvitationOutBound() {
        val res: ResponseEnvelope
        try {
            val dataInvitation =
                "{\"invitation\":{\"@id\":\"1384ca2e-2f28-4990-b694-f0eea1c6b176\",\"@type\":\"https://didcomm.org/out-of-band/1.0/invitation\",\"label\":\"Mediador\",\"services\":[{\"id\":\"b70af629-f643-4197-acdf-303634a22543\",\"type\":\"did-communication\",\"recipientKeys\":[\"did:key:z6MkjD3SLFwnNexGYx2JN2hzLSzy171tw7rg4kretDSYEwj6\"],\"serviceEndpoint\":\"ws://192.168.1.11:8091\"}],\"accept\":[\"didcomm/aip2;env=rfc19\"],\"handshake_protocols\":[\"https://didcomm.org/didexchange/1.0\"]},\"my_label\":\"mediador\"}"
            val data: ByteArray =
                dataInvitation.toByteArray(StandardCharsets.UTF_8)
            val requestEnvelope = RequestEnvelope(data)
            val didex = agent.outOfBandController
            res = didex.acceptInvitation(requestEnvelope)
            if (res.error != null && res.error.message.isNotEmpty()) {
                val error = res.error.message
                Log.e("aceptarInvitationOutBound", error)
            } else {
                val receiveInvitationResponse = String(res.payload, StandardCharsets.UTF_8)
                connectionIdRegisterMediator = receiveInvitationResponse
                Log.i("aceptarInvitationOutBound", receiveInvitationResponse)

            }
        } catch (e: Exception) {
            Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            Log.e("aceptarInvitationOutBound", e.message.toString())
        }
    }

    private fun acceptInvitationOutBoundAgent() {
        val dataModel = gson.fromJson(connectionIdRegisterMediator, InvitationOutBand::class.java )
        val res: ResponseEnvelope
        try {
            val dataInvitation =
                "{\"invitation\":{\"@id\":\"4366513f-7b83-411f-a636-327710343ec1\",\"@type\":\"https://didcomm.org/out-of-band/1.0/invitation\",\"label\":\"Agente EPM\",\"services\":[{\"id\":\"1bf1f0b0-3644-412a-b04a-e1ae8e850284\",\"type\":\"did-communication\",\"recipientKeys\":[\"did:key:z6Mknf4B4qViedzEGgmUXb1uDXdG7AZPL8AEQqPAp6rxLCwe\"],\"serviceEndpoint\":\"ws://192.168.1.11:9094\"}],\"accept\":[\"didcomm/aip2;env=rfc19\"],\"handshake_protocols\":[\"https://didcomm.org/didexchange/1.0\"]},\"my_label\":\"AgenteEPM\",\"router_connections\":\"${dataModel.connectionId}\"}"
            val data: ByteArray =
                dataInvitation.toByteArray(StandardCharsets.UTF_8)
            val requestEnvelope = RequestEnvelope(data)
            val didex = agent.outOfBandController
            res = didex.acceptInvitation(requestEnvelope)
            if (res.error != null && res.error.message.isNotEmpty()) {
                val error = res.error.message
                Log.e("acceptInvitationOutBoundAgent", error)
            } else {
                val paylod = String(res.payload, StandardCharsets.UTF_8)
                Log.i("acceptInvitationOutBoundAgent", paylod)

            }
        } catch (e: Exception) {
            Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            Log.e("acceptInvitationOutBoundAgent", e.message.toString())
        }
    }

    private fun registerMediator() {
        val dataModel = gson.fromJson(connectionIdRegisterMediator, InvitationOutBand::class.java )
        val res: ResponseEnvelope
        try {
            val dataInvitation = "{\"connectionID\":\"${dataModel.connectionId}\"}"
            val data: ByteArray =
                dataInvitation.toByteArray(StandardCharsets.UTF_8)
            val requestEnvelope = RequestEnvelope(data)
            val mediator = agent.mediatorController
            res = mediator.register(requestEnvelope)
            if (res.error != null && res.error.message.isNotEmpty()) {
                val error = res.error.message
                Log.e("registerMediator", error)
            } else {
                val registerMediator = String(res.payload, StandardCharsets.UTF_8)
                Log.e("registerMediator", registerMediator)

            }
        } catch (e: Exception) {
            Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            Log.e("aceptarInvitationOutBound", e.message.toString())
        }
    }

    private fun getStatus() {
        val res: ResponseEnvelope
        try {
            val data: ByteArray = "{}".toByteArray()
            val requestEnvelope = RequestEnvelope(data)
            val didex = agent.didExchangeController
            res = didex.queryConnections(requestEnvelope)
            if (res.error != null && !res.error.message.isEmpty()) {
                val error = res.error.message
                Log.e("getMediator", error)
            } else {
                val receiveInvitationResponse = String(res.payload, StandardCharsets.UTF_8)
                Log.i("getMediator", receiveInvitationResponse)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            Log.e("receiveInvitationMediator", e.message.toString())
        }
    }


    /**
     * Protocolo de mensajes
     *
     */
    private suspend fun sendMessage() = withContext(Dispatchers.IO) {
        var res = ResponseEnvelope()
        try {

            // create a controller
            val v = agent.messagingController

            // perform an operation
            val data = "{ \"content\": \"Hello\" }".toByteArray(StandardCharsets.UTF_8)
            res = v.send(RequestEnvelope(data))
        } catch (e: Exception) {
            Log.e("GetCredential", e.localizedMessage)
        }
        if (res.error != null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            }
            if (res.error.message != "") {
                println(res.error.message)
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, String(res.payload, StandardCharsets.UTF_8), Toast.LENGTH_LONG)
                .show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
