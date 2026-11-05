package com.example.aplicativocicli.alarm
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.aplicativocicli.data.Alarm
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.main
import java.time.LocalDate
import java.util.Calendar

object alarme_agenda {
    private const val OFFSET_REPETICAO = 100000

    fun agendarRepeticao(context: Context, extras: Bundle) {
        val alarmId = extras.getInt("alarm_id")
        val novo = Bundle(extras).apply { putInt("attempt", extras.getInt("attempt") + 1) }
        val quando = System.currentTimeMillis() + extras.getInt("interval") * 60_000L
        schedule(context, quando, novo, alarmId + OFFSET_REPETICAO)
    }

    fun cancelarRepeticao(context: Context, alarmId: Int) {
        cancel(context, alarmId + OFFSET_REPETICAO)
    }
    fun schedule(context: Context, triggerAt: Long, extras: Bundle, requestCode: Int) {
        val am = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java).putExtras(extras)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val show = PendingIntent.getActivity(context, requestCode, Intent(context, main::class.java), PendingIntent.FLAG_IMMUTABLE)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, show), pi)
    }

    fun cancel(context: Context, requestCode: Int) {
        val am = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
    }

    fun nextTime(hour: Int, minute: Int): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1)
        return c.timeInMillis
    }

    suspend fun agendar(context: Context, a: Alarm, amanha: Boolean = false) {
        val cartela = AppDatabase.get(context).pillPackDao().cartelaAtual()
        val extras = Bundle().apply {
            putInt("alarm_id", a.id.toInt())
            putInt("hour", a.hour)
            putInt("minute", a.minute)
            putInt("attempt", 0)
            putInt("interval", a.intervalMinutes)
            putInt("limit", a.repeatLimit)
            putString("start_date", cartela?.startDate ?: LocalDate.now().toString())
            putInt("active", cartela?.activeDays ?: 28)
            putInt("placebo", cartela?.placeboDays ?: 0)
            putInt("pause", cartela?.pauseDays ?: 0)
        }
        val quando = nextTime(a.hour, a.minute) + if (amanha) 86_400_000L else 0L
        schedule(context, quando, extras, a.id.toInt())
    }
}