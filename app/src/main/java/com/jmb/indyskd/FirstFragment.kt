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
import com.jmb.indyskd.models.CreateInvitationResponse
import com.jmb.indyskd.models.Invitation
import com.jmb.indyskd.models.ReceiveInvitation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.hyperledger.aries.api.AriesController
import org.hyperledger.aries.ariesagent.Ariesagent
import org.hyperledger.aries.config.Options
import org.hyperledger.aries.models.RequestEnvelope
import org.hyperledger.aries.models.ResponseEnvelope
import java.nio.charset.StandardCharsets


class FirstFragment : Fragment() {


    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var agent: AriesController
    private lateinit var handler: MyHandler
    private val agentURL = "192.168.0.12"
    var gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            createAgent()
        }

        binding.btnCreateInvitation.setOnClickListener {
            GlobalScope.launch {
                createInvitation()
            }
        }

        binding.btnConnectMediator.setOnClickListener {
            GlobalScope.launch {
                registerMediator()
            }
        }

    }


    private suspend fun createAgent() = withContext(Dispatchers.IO) {
        val opts = Options()
        opts.agentURL =
            "http://$agentURL:8021"
        //opts.websocketURL = "ws://$agentURL:8030"
        opts.label = "EPM Agent"
        opts.autoAccept = true
        opts.addOutboundTransport("ws")
        opts.transportReturnRoute = "all"
        opts.useLocalAgent = false

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
     * Conexion con el agente
     *
     */

    private suspend fun createInvitation() = withContext(Dispatchers.IO) {
        val reqData = ""
        val requestEnvelope = RequestEnvelope(reqData.toByteArray(StandardCharsets.UTF_8))
        try {
            val didex = agent.didExchangeController
            val res = didex.createInvitation(requestEnvelope)
            if (res.error != null) {
                val error = res.error.message
                Log.e("createInvitation", error)
            } else {
                val acceptedInvitation = String(res.payload)
                val newInvitation = acceptedInvitation.replace("0.0.0.0",agentURL)
                val json = gson.fromJson(newInvitation, CreateInvitationResponse::class.java)
                Log.i("createInvitation", json.toString())
                receiveInvitation(json.invitation)

            }
        } catch (e: Exception) {
            Log.e("createInvitation", e.message.toString())
        }
    }

    private suspend fun receiveInvitation(dataInvitation: Invitation) = withContext(Dispatchers.IO) {
        val reqData = "{}"
        val res: ResponseEnvelope
        try {
            // call did exchange method
            val data: ByteArray = gson.toJson(dataInvitation).toByteArray()
            val requestEnvelope = RequestEnvelope(data)
            val didex = agent.didExchangeController
            res = didex.receiveInvitation(requestEnvelope)
            if (res.error != null && res.error.message.isNotEmpty()) {
                val error = res.error.message
                Log.e("ReceiveInvitation", error)
            } else {
                val receiveInvitationResponse = String(res.payload, StandardCharsets.UTF_8)
                val json = gson.fromJson(receiveInvitationResponse, ReceiveInvitation::class.java)
                Log.i("ReceiveInvitation", json.toString())
                acceptInvitation(json)
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(context, "You need create an agent", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }
    private suspend fun acceptInvitation(invitation: ReceiveInvitation) = withContext(Dispatchers.IO) {

        val requestEnvelope = RequestEnvelope(invitation.connectionId.toByteArray(StandardCharsets.UTF_8))
        try {
            val didex = agent.didExchangeController
            val res = didex.acceptInvitation(requestEnvelope)
            if (res.error != null) {
                val error = res.error.message
                Log.e("AcceptInvitation", error)
            } else {
                val acceptedInvitation = String(res.payload, StandardCharsets.UTF_8)
                Log.i("AcceptInvitation", acceptedInvitation)
            }
        } catch (e: Exception) {
            Log.e("AcceptInvitation", e.message.toString())
        }

    }

    private suspend fun getCredentials() = withContext(Dispatchers.IO) {
        var res = ResponseEnvelope()
        try {

            // create a controller
            val v = agent.verifiableController

            // perform an operation
            val data = "".toByteArray(StandardCharsets.UTF_8)
            res = v.getCredentials(RequestEnvelope(data))
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


    /**
     * Conexion con el mediador
     *
     */
    private suspend fun registerMediator() = withContext(Dispatchers.IO) {
        val reqData =
            "{\"@type\":\"https://didcomm.org/connections/1.0/invitation\",\"@id\":\"a1373d71-3094-46fe-95e2-5394e25dae44\",\"label\":\"Aries Framework JavaScript Mediator\",\"recipientKeys\":[\"9Q7fjjKCXuhQbYHgpBUSw9ApLA5UNKVbZEBoq37qTmgs\"],\"serviceEndpoint\":\"https://$agentURL:3001\",\"routingKeys\":[]}"
        val requestEnvelope = RequestEnvelope(reqData.toByteArray(StandardCharsets.UTF_8))
        try {
            val mediator = agent.mediatorController
            val res = mediator.register(requestEnvelope)
            if (res.error != null) {
                val error = res.error.message
                Log.e("registerMediator", error)
            } else {
                val payload = String(res.payload, StandardCharsets.UTF_8)
                Log.i("registerMediator", payload)
            }
        } catch (e: Exception) {
            Log.e("registerMediator", e.message.toString())
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
