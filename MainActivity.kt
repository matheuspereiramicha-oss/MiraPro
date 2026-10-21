package com.mirapro.app

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private val bg = 0xFF101114.toInt()
    private val panel = 0xFF1B1D22.toInt()
    private val red = 0xFFFF4545.toInt()
    private val white = 0xFFF5F5F7.toInt()
    private val muted = 0xFFB0B3BB.toInt()
    private lateinit var db: ProfileDb
    private lateinit var root: LinearLayout
    private lateinit var game: Spinner
    private lateinit var style: Spinner
    private lateinit var fingers: Spinner
    private lateinit var dpiMode: Spinner
    private lateinit var dpi: EditText
    private lateinit var notes: EditText
    private lateinit var results: LinearLayout
    private var last: Config? = null

    data class Config(val game: String, val style: String, val fingers: Int, val dpiMode: String,
        val dpi: Int?, val general: Int, val red: Int, val x2: Int, val x4: Int,
        val awm: Int, val look: Int, val fire: Int, val device: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ProfileDb(this)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(26)) }
        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(txt("MIRA", 29f, white, true))
        header.addView(txt("PRO", 17f, red, true).apply { setPadding(dp(7), dp(5), 0, 0) })
        header.addView(txt("BETA • ANDROID", 10f, muted, true).apply { gravity = Gravity.END }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(header)
        add(root, "Configuração inteligente para Free Fire", 15f, muted, false, 8)
        add(root, "Seu aparelho. Seu ajuste. Seus testes.", 23f, white, true, 12)

        val device = card(); root.addView(device)
        add(device, "APARELHO DETECTADO", 11f, red, true, 7)
        add(device, "${Build.MANUFACTURER} ${Build.MODEL}", 18f, white, true, 5)
        add(device, "Marca: ${Build.BRAND} • Modelo: ${Build.DEVICE}", 12f, muted, false, 4)
        add(device, "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", 12f, muted, false, 4)
        val dm = resources.displayMetrics
        val hz = try { if (Build.VERSION.SDK_INT >= 30) display?.mode?.refreshRate?.roundToInt()?.toString() ?: "indisponível" else "não exposto com confiança" } catch (_: Exception) { "indisponível" }
        add(device, "Tela: ${dm.widthPixels} × ${dm.heightPixels} px • densidade lógica: ${(dm.density * 160).roundToInt()} dpi", 12f, muted, false, 4)
        add(device, "Taxa reportada: $hz Hz", 12f, muted, false, 4)
        add(device, "FPS real do jogo, taxa de toque e alguns dados de RAM/CPU não são acessíveis a todos os apps.", 11f, muted, false, 0)

        val form = card(); root.addView(form)
        add(form, "PERSONALIZE SEU PERFIL", 14f, white, true, 10)
        add(form, "Jogo", 12f, muted, false, 3)
        game = spinner(listOf("Free Fire MAX", "Free Fire")); form.addView(game)
        add(form, "Estilo de jogo", 12f, muted, false, 3)
        style = spinner(listOf("Equilibrado", "Puxada de capa", "Precisão / rajada", "Curta distância", "AWM / longa distância")); form.addView(style)
        add(form, "HUD", 12f, muted, false, 3)
        fingers = spinner(listOf("2 dedos", "3 dedos", "4 dedos", "5 dedos")); form.addView(fingers)
        add(form, "DPI", 12f, muted, false, 3)
        dpiMode = spinner(listOf("Sem alterar DPI", "Usar minha DPI atual", "Quero testar outra DPI")); form.addView(dpiMode)
        dpi = EditText(this).apply {
            hint = "DPI atual (opcional; ex.: 420)"; setHintTextColor(muted); setTextColor(white)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER; textSize = 14f
            setPadding(dp(12), dp(8), dp(12), dp(8)); background = rounded(panel, 10)
        }
        form.addView(dpi, LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(6); bottomMargin = dp(8) })
        add(form, "Resultado da última sessão", 12f, muted, false, 3)
        notes = EditText(this).apply {
            hint = "Ex.: passou da cabeça / ficou abaixo / tremeu"; setHintTextColor(muted); setTextColor(white)
            textSize = 14f; minLines = 2; gravity = Gravity.TOP
            setPadding(dp(12), dp(10), dp(12), dp(10)); background = rounded(panel, 10)
        }
        form.addView(notes, LinearLayout.LayoutParams(-1, dp(72)))
        form.addView(button("GERAR CONFIGURAÇÃO PERSONALIZADA"), LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(12) })
        (form.getChildAt(form.childCount - 1) as Button).setOnClickListener { generate() }

        results = card(); root.addView(results)
        add(results, "SUA CONFIGURAÇÃO", 14f, white, true, 6)
        add(results, "Escolha suas preferências e toque em gerar.", 13f, muted, false, 0)

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val save = button("SALVAR PERFIL")
        val history = button("HISTÓRICO")
        row.addView(save, LinearLayout.LayoutParams(0, dp(46), 1f).apply { rightMargin = dp(5) })
        row.addView(history, LinearLayout.LayoutParams(0, dp(46), 1f).apply { leftMargin = dp(5) })
        root.addView(row)
        save.setOnClickListener {
            val c = last
            if (c == null) toast("Gere uma configuração antes de salvar.")
            else { db.save(c, notes.text.toString()); toast("Perfil salvo neste aparelho.") }
        }
        history.setOnClickListener { showHistory() }

        val info = card(); root.addView(info)
        add(info, "IMPORTANTE", 12f, red, true, 6)
        add(info, "Os valores são regras iniciais, não uma promessa de capa automático. O app não muda a DPI sozinho, não altera o jogo e não lê FPS real do Free Fire. Teste no treinamento e mude uma variável por vez.", 12f, muted, false, 0)
    }

    private fun generate() {
        val st = style.selectedItem.toString()
        val f = fingers.selectedItemPosition + 2
        val mode = dpiMode.selectedItem.toString()
        val inputDpi = dpi.text.toString().toIntOrNull()?.takeIf { it in 200..800 }
        val currentDpi = (resources.displayMetrics.density * 160).roundToInt()
        val displayBonus = if (Build.VERSION.SDK_INT >= 31 && resources.displayMetrics.widthPixels >= 1080) 2 else 0
        var g = 175 + displayBonus; var r = 168 + displayBonus; var x2 = 158 + displayBonus
        var x4 = 145 + displayBonus; var awm = 95; var look = 135
        var fire = when (f) { 2 -> 48; 3 -> 46; 4 -> 43; else -> 41 }
        when (st) {
            "Puxada de capa" -> { g += 8; r += 8; x2 += 5; x4 += 2; awm -= 5; fire += 2 }
            "Precisão / rajada" -> { g -= 12; r -= 14; x2 -= 10; x4 -= 8; awm += 5; fire -= 2 }
            "Curta distância" -> { g += 10; r += 6; x2 += 1; x4 -= 5; look += 5; fire += 3 }
            "AWM / longa distância" -> { g -= 18; r -= 12; x2 -= 12; x4 -= 10; awm += 12; look -= 5; fire -= 3 }
        }
        if (f >= 4) { g -= 3; r -= 2 }
        if (mode == "Quero testar outra DPI" && inputDpi != null) {
            val delta = (inputDpi - currentDpi) / 40
            g -= delta / 2; r -= delta / 2
        }
        val c = Config(game.selectedItem.toString(), st, f, mode, inputDpi,
            g.coerceIn(0, 200), r.coerceIn(0, 200), x2.coerceIn(0, 200), x4.coerceIn(0, 200),
            awm.coerceIn(0, 200), look.coerceIn(0, 200), fire.coerceIn(25, 70), "${Build.MANUFACTURER} ${Build.MODEL}")
        last = c
        results.removeAllViews()
        add(results, "BASE CALCULADA • ${c.game}", 11f, red, true, 4)
        add(results, "Estilo: ${c.style} • HUD: ${c.fingers} dedos", 12f, muted, false, 6)
        listOf("Geral" to c.general, "Ponto vermelho" to c.red, "Mira 2×" to c.x2,
            "Mira 4×" to c.x4, "Mira AWM" to c.awm, "Olhadinha" to c.look).forEach { (label, value) ->
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(7), 0, dp(7)) }
            line.addView(txt(label, 14f, white, false), LinearLayout.LayoutParams(0, -2, 1f))
            line.addView(txt(value.toString(), 18f, red, true))
            results.addView(line)
            results.addView(android.view.View(this).apply { setBackgroundColor(0xFF303239.toInt()) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        add(results, "Botão de atirar: ${c.fire}% • Gráficos: Suave/Padrão • FPS: maior opção estável disponível", 13f, white, true, 7)
        val dpiText = when (mode) {
            "Sem alterar DPI" -> "DPI: mantenha a atual; é o ponto de partida mais seguro."
            "Usar minha DPI atual" -> "DPI informada: ${inputDpi ?: "não informada"}. O app não a altera."
            else -> "DPI atual informada: ${inputDpi ?: "não informada"}; densidade lógica: $currentDpi. Mude em passos pequenos e reverta se a interface ficar desconfortável."
        }
        add(results, dpiText, 12f, muted, false, 7)
        add(results, "Método: regras fixas por estilo, dedos e alguns dados da tela/Android; não há sorteio. Faça 3 sessões curtas e ajuste 3–5 pontos conforme o resultado observado.", 12f, muted, false, 0)
        toast("Configuração calculada sem sorteio.")
    }

    private fun showHistory() {
        val rows = db.history()
        val msg = if (rows.isEmpty()) "Nenhum perfil salvo ainda." else rows.joinToString("\n\n") { "${it.first}\n${it.second}" }
        android.app.AlertDialog.Builder(this).setTitle("Histórico de perfis").setMessage(msg).setPositiveButton("OK", null).show()
    }

    private fun spinner(items: List<String>): Spinner {
        val s = Spinner(this)
        val a = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View =
                (super.getView(position, convertView, parent) as TextView).apply { setTextColor(white); textSize = 14f }
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View =
                (super.getDropDownView(position, convertView, parent) as TextView).apply { setTextColor(white); setBackgroundColor(panel); setPadding(dp(12), dp(10), dp(12), dp(10)) }
        }
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); s.adapter = a
        s.background = rounded(panel, 10); s.setPadding(dp(8), 0, dp(8), 0)
        s.layoutParams = LinearLayout.LayoutParams(-1, dp(46)).apply { bottomMargin = dp(7) }
        return s
    }
    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(15), dp(15), dp(15)); background = rounded(panel, 16)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) }
    }
    private fun add(parent: LinearLayout, text: String, size: Float, color: Int, bold: Boolean, bottom: Int) {
        parent.addView(txt(text, size, color, bold), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(bottom) })
    }
    private fun txt(text: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD
        setLineSpacing(dp(2).toFloat(), 1f)
    }
    private fun button(text: String) = Button(this).apply {
        this.text = text; textSize = 12f; setTextColor(Color.WHITE); isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD; background = rounded(red, 12); stateListAnimator = null
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).roundToInt()
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    override fun onDestroy() { if (::db.isInitialized) db.close(); super.onDestroy() }
}

private class ProfileDb(context: Context) : SQLiteOpenHelper(context, "mirapro.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE profiles (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, device TEXT, game TEXT, style TEXT, fingers INTEGER, dpi_mode TEXT, dpi INTEGER, general INTEGER, red_dot INTEGER, scope2 INTEGER, scope4 INTEGER, sniper INTEGER, free_look INTEGER, fire_size INTEGER, notes TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun save(c: MainActivity.Config, notes: String) {
        val v = ContentValues()
        v.put("device", c.device); v.put("game", c.game); v.put("style", c.style); v.put("fingers", c.fingers)
        v.put("dpi_mode", c.dpiMode); if (c.dpi == null) v.putNull("dpi") else v.put("dpi", c.dpi)
        v.put("general", c.general); v.put("red_dot", c.red); v.put("scope2", c.x2); v.put("scope4", c.x4)
        v.put("sniper", c.awm); v.put("free_look", c.look); v.put("fire_size", c.fire); v.put("notes", notes)
        writableDatabase.insert("profiles", null, v)
    }
    fun history(): List<Pair<String, String>> {
        val out = mutableListOf<Pair<String, String>>()
        readableDatabase.rawQuery("SELECT created_at, device, style, fingers, general, red_dot, scope2, scope4, sniper, fire_size, notes FROM profiles ORDER BY id DESC LIMIT 20", null).use { c ->
            while (c.moveToNext()) {
                out.add("${c.getString(0)} • ${c.getString(1)}" to
                    "${c.getString(2)} • HUD ${c.getInt(3)} dedos\nGeral ${c.getInt(4)} | Ponto vermelho ${c.getInt(5)} | 2× ${c.getInt(6)} | 4× ${c.getInt(7)} | AWM ${c.getInt(8)} | Botão ${c.getInt(9)}%\n${c.getString(10).orEmpty()}")
            }
        }
        return out
    }
}
