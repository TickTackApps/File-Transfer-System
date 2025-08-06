package com.example.localdb1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.localdb1.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ip = intent.getStringExtra("ip")
        val pass = intent.getStringExtra("pass")

        if (ip != null){
            put(ip)
            binding.pass.setText(pass)
        }



        binding.ipLayout.isVisible = true
        binding.hostname.isVisible = false

        binding.scanner.setOnClickListener {

            val intent = Intent(this@SettingsActivity, QRscanAct::class.java)
            startActivity(intent)
            finish()

        }
        var isPasswordVisible = false

        binding.pass.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.pass.right - binding.pass.compoundPaddingEnd)) {
                    isPasswordVisible = !isPasswordVisible

                    if (isPasswordVisible) {
                        binding.pass.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_remove_24, 0)
                    } else {
                        binding.pass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_remove_red_eye_24, 0)
                    }


                    binding.pass.setSelection(binding.pass.text.length)


                    view.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }


        binding.pass.setOnClickListener {

        }

        binding.pass.performClick()




        binding.ip1.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()

                // Avoid crashing when the EditText is empty
                if (text.isEmpty()) return

                try {
                    val number = text.toInt()
                    if (number > 255) {
                        binding.ip1.setText("255")
                        binding.ip1.setSelection(binding.ip1.text.length) // Keep cursor at the end
                        Toast.makeText(
                            this@SettingsActivity,
                            "Enter number in range of 0-255",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace() // Log the error instead of crashing
                }
            }

        })
        binding.ip2.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()

                // Avoid crashing when the EditText is empty
                if (text.isEmpty()) return

                try {
                    val number = text.toInt()
                    if (number > 255) {
                        binding.ip2.setText("255")
                        binding.ip2.setSelection(binding.ip2.text.length) // Keep cursor at the end
                        Toast.makeText(
                            this@SettingsActivity,
                            "Enter number in range of 0-255",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace() // Log the error instead of crashing
                }
            }

        })
        binding.ip3.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()

                // Avoid crashing when the EditText is empty
                if (text.isEmpty()) return

                try {
                    val number = text.toInt()
                    if (number > 255) {
                        binding.ip3.setText("255")
                        binding.ip3.setSelection(binding.ip3.text.length) // Keep cursor at the end
                        Toast.makeText(
                            this@SettingsActivity,
                            "Enter number in range of 0-255",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace() // Log the error instead of crashing
                }
            }

        })
        binding.ip4.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()

                // Avoid crashing when the EditText is empty
                if (text.isEmpty()) return

                try {
                    val number = text.toInt()
                    if (number > 255) {
                        binding.ip4.setText("255")
                        binding.ip4.setSelection(binding.ip4.text.length) // Keep cursor at the end
                        Toast.makeText(
                            this@SettingsActivity,
                            "Enter number in range of 0-255",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace() // Log the error instead of crashing
                }
            }

        })

        binding.navigator.setOnItemSelectedListener {

            when(it.itemId){

                R.id.ip_nav -> navigation("ip")

                R.id.host_nav -> navigation("host")

                else -> {

                }



            }

            return@setOnItemSelectedListener true

        }




    }

    private fun put(ip: String?) {

        for (i in 1..4) {

            var part = ip!!.take(i*3).drop((i-1)*3)

            if (i == 1){
                binding.ip1.setText(part)
            }else if (i==2){
                binding.ip2.setText(part)
            }else if (i == 3){
                binding.ip3.setText(part)
            }else{
                binding.ip4.setText(part)
            }

        }

    }

    fun navigation( nav:String ){

        if (nav == "ip"){

            binding.ipLayout.isVisible = true
            binding.hostname.isVisible = false

        }
        else if (nav == "host"){
            binding.ipLayout.isVisible = false
            binding.hostname.isVisible = true
        }

    }


}