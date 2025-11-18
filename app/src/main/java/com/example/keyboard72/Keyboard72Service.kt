package com.example.keyboard72

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast

class Keyboard72Service : InputMethodService() {

    private var inputView: View? = null
    private lateinit var previewText: TextView
    private lateinit var keysGrid: GridLayout
    private var mapping = listOf<KeyDef>()

    override fun onCreate() {
        super.onCreate()
        mapping = KeyMappingManager.loadMapping(this)
    }

    override fun onCreateInputView(): View {
        val inflater = layoutInflater
        inputView = inflater.inflate(R.layout.input_view, null)
        previewText = inputView!!.findViewById(R.id.preview_text)
        keysGrid = inputView!!.findViewById(R.id.keys_grid)
        buildKeys()
        return inputView!!
    }

    private fun buildKeys() {
        keysGrid.removeAllViews()
        keysGrid.columnCount = 6
        mapping = KeyMappingManager.loadMapping(this)
        mapping.forEach { keyDef ->
            val btn = Button(this)
            btn.text = keyDef.label
            btn.textSize = 18f
            btn.setOnClickListener {
                sendKey(keyDef.label)
            }
            btn.setOnLongClickListener {
                SettingsActivity.startForEdit(this, keyDef.id)
                true
            }
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(6,6,6,6)
            btn.layoutParams = params
            keysGrid.addView(btn)
        }
    }

    private fun sendKey(label: String) {
        val ic: InputConnection? = currentInputConnection
        if (ic != null) {
            ic.commitText(label, 1)
            previewText.text = label
        } else {
            Toast.makeText(this, "No input connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        mapping = KeyMappingManager.loadMapping(this)
        if (inputView != null) buildKeys()
    }
}