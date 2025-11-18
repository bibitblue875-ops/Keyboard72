package com.example.keyboard72

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class KeyDef(val id: Int, val label: String, val caption: String)

object KeyMappingManager {

    private const val PREF = "keyboard72_prefs"
    private const val KEY_MAP = "key_mapping_json"

    fun defaultMapping(): List<KeyDef> {
        val defaults = mutableListOf<KeyDef>()

        // 4 huruf inti
        defaults.add(KeyDef(1, "اَ", "A (Zat)"))
        defaults.add(KeyDef(2, "لَ", "L (Sifat)"))
        defaults.add(KeyDef(3, "لٌ", "L (Nama)"))
        defaults.add(KeyDef(4, "ەُ", "H (Gerak)"))

        // Sisa tombol placeholder sampai 72
        for (i in 5..72) {
            defaults.add(KeyDef(i, "K%02d".format(i), "Key %02d".format(i)))
        }

        return defaults
    }

    fun saveMapping(context: Context, mapping: List<KeyDef>) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray()

        mapping.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("label", it.label)
            obj.put("caption", it.caption)
            arr.put(obj)
        }

        prefs.edit().putString(KEY_MAP, arr.toString()).apply()
    }

    fun loadMapping(context: Context): List<KeyDef> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_MAP, null)

        if (saved == null) return defaultMapping()

        return try {
            val arr = JSONArray(saved)
            val out = mutableListOf<KeyDef>()

            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    KeyDef(
                        o.getInt("id"),
                        o.getString("label"),
                        o.optString("caption", "")
                    )
                )
            }

            if (out.size != 72) defaultMapping() else out
        } catch (e: Exception) {
            defaultMapping()
        }
    }

    fun exportMappingJson(context: Context): String {
        val mapping = loadMapping(context)
        val arr = JSONArray()

        mapping.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("label", it.label)
            obj.put("caption", it.caption)
            arr.put(obj)
        }

        return arr.toString(2)
    }

    fun importMappingJson(context: Context, json: String): Boolean {
        return try {
            val arr = JSONArray(json)
            if (arr.length() != 72) return false

            val mapping = mutableListOf<KeyDef>()

            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                mapping.add(
                    KeyDef(
                        o.getInt("id"),
                        o.getString("label"),
                        o.optString("caption", "")
                    )
                )
            }

            saveMapping(context, mapping)
            true

        } catch (e: Exception) {
            false
        }
    }
}