package com.example.keyboard72

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    companion object {
        fun startForEdit(context: Context, keyId: Int) {
            val i = Intent(context, SettingsActivity::class.java)
            i.putExtra("edit_id", keyId)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Keyboard72 Settings"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16,16,16,16)
        }
        setContentView(root)

        val info = TextView(this)
        info.text = "Aktifkan keyboard dari Pengaturan → Bahasa & Input → Keyboard Virtual → Kelola Keyboard."
        root.addView(info)

        val btnExport = Button(this).apply { text = "Export Mapping (JSON)" }
        val btnImport = Button(this).apply { text = "Import Mapping (paste JSON)" }
        val btnReset = Button(this).apply { text = "Reset Default Mapping" }

        root.addView(btnExport)
        root.addView(btnImport)
        root.addView(btnReset)

        btnExport.setOnClickListener {
            val json = KeyMappingManager.exportMappingJson(this)
            val share = Intent(Intent.ACTION_SEND)
            share.type = "application/json"
            share.putExtra(Intent.EXTRA_TEXT, json)
            startActivity(Intent.createChooser(share, "Export mapping"))
        }

        btnImport.setOnClickListener {
            val edit = EditText(this)
            edit.minLines = 8
            AlertDialog.Builder(this)
                .setTitle("Paste mapping JSON:")
                .setView(edit)
                .setPositiveButton("Import") { _, _ ->
                    val ok = KeyMappingManager.importMappingJson(this, edit.text.toString())
                    if (ok) {
                        Toast.makeText(this, "Import Berhasil", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal: JSON tidak valid", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Mapping?")
                .setMessage("Kembalikan seluruh tombol ke versi default?")
                .setPositiveButton("Ya") { _, _ ->
                    KeyMappingManager.saveMapping(this, KeyMappingManager.defaultMapping())
                    Toast.makeText(this, "Reset selesai", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Tidak", null)
                .show()
        }

        val editId = intent.getIntExtra("edit_id", -1)
        if (editId > 0) {
            showEditKeyDialog(editId)
        }
    }

    private fun showEditKeyDialog(keyId: Int) {
        val mapping = KeyMappingManager.loadMapping(this)
        val key = mapping.find { it.id == keyId } ?: return

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val etLabel = EditText(this)
        etLabel.setText(key.label)

        val etCaption = EditText(this)
        etCaption.setText(key.caption)

        layout.addView(TextView(this).apply { text = "Label tombol:" })
        layout.addView(etLabel)
        layout.addView(TextView(this).apply { text = "Caption:" })
        layout.addView(etCaption)

        AlertDialog.Builder(this)
            .setTitle("Edit Key #$keyId")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val newLabel = etLabel.text.toString()
                val newCaption = etCaption.text.toString()
                val newMapping = mapping.map {
                    if (it.id == keyId) KeyDef(it.id, newLabel, newCaption) else it
                }
                KeyMappingManager.saveMapping(this, newMapping)
                Toast.makeText(this, "Simpan selesai", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Batal") { _, _ -> finish() }
            .show()
    }
}