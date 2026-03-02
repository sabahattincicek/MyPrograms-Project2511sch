package com.saboon.project_2511sch.util

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.util.Locale

class CharacterManager(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // Karakterleri bellekte tutacağımız liste
    val allCharacters: List<Character>

    init {
        // Sınıf ilk oluşturulduğunda tüm karakterleri yükle
        allCharacters = loadAllCharactersFromAssets()
        Log.d("CharacterManager", "Total ${allCharacters.size} characters loaded into memory.")
    }

    /**
     * assets/characters klasöründeki tüm karakterleri ilk açılışta bir kez okur.
     */
    private fun loadAllCharactersFromAssets(): List<Character> {
        val tempCharacters = mutableListOf<Character>()
        try {
            val characterFolders = context.assets.list("characters") ?: return emptyList()

            for (charId in characterFolders) {
                try {
                    val fileName = "characters/$charId/$charId.json"
                    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                    val character = json.decodeFromString<Character>(jsonString)
                    tempCharacters.add(character)
                } catch (e: Exception) {
                    Log.e("CharacterManager", "Error loading character: $charId", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CharacterManager", "Error listing characters directory", e)
        }
        return tempCharacters
    }


    /**
     * Bellekteki (RAM) liste içinden ID'ye göre karakteri bulur.
     */
    fun getCharacter(charId: String): Character? {
        return allCharacters.find { it.id == charId }
    }

    /**
     * Mevcut sistem diline göre mesajı veya ismi döndüren yardımcı fonksiyon.
     */
    fun getLocalizedText(textMap: Map<String, String>): String {
        val currentLang = Locale.getDefault().language
        return textMap[currentLang] ?: textMap["en"] ?: ""
    }
}