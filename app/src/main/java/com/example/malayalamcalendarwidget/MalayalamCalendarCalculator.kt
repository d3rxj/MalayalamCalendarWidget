package com.example.malayalamcalendarwidget

import java.util.Calendar
import kotlin.math.floor
import kotlin.math.sin

/**
 * Computes the Malayalam (Kollavarsham) solar calendar date for a given Gregorian date.
 *
 * The Malayalam calendar is a SIDEREAL SOLAR calendar: each of its 12 months begins the
 * moment the Sun's sidereal longitude (i.e. tropical longitude minus the ayanamsa/precession
 * offset) crosses a multiple of 30 degrees. This is fundamentally different from the
 * Gregorian calendar, so there is no fixed date mapping - it has to be calculated.
 *
 * This uses:
 *  - A standard low-precision solar position formula (Sun's apparent tropical longitude,
 *    accurate to a small fraction of a degree - plenty for calendar purposes).
 *  - A linear approximation of the Lahiri ayanamsa (the ayanamsa most commonly used for
 *    Malayalam/Tamil solar calendars).
 *
 * Because both formulas are approximations, the calculated date can occasionally be off by
 * one day right at a month boundary (Sankranti) compared to an official published Panchangam.
 * For everyday use on a home screen widget this is expected to match almost always.
 */
object MalayalamCalendarCalculator {

    // Index 0 = Mesham ... Index 11 = Meenam (standard zodiac/rashi order)
    private val malayalamMonths = arrayOf(
        "മേടം", "ഇടവം", "മിഥുനം", "കർക്കടകം", "ചിങ്ങം", "കന്നി",
        "തുലാം", "വൃശ്ചികം", "ധനു", "മകരം", "കുംഭം", "മീനം"
    )

    data class MalayalamDate(val monthName: String, val day: Int, val kollavarshamYear: Int)

    fun getMalayalamDate(calendar: Calendar = Calendar.getInstance()): MalayalamDate {
        val jd = julianDayAtLocalNoon(calendar)
        val rashiIndexToday = rashiIndex(jd)

        // Walk backwards day by day until the rashi changes, to find the day-of-month count.
        var day = 1
        var probeJd = jd - 1.0
        var guard = 0
        while (rashiIndex(probeJd) == rashiIndexToday && guard < 40) {
            day++
            probeJd -= 1.0
            guard++
        }

        val monthName = malayalamMonths[rashiIndexToday]

        // Malayalam month number where Chingam (rashi index 4, Simha) = 1 ... Karkidakam = 12
        val mlMonthNum = ((rashiIndexToday - 4 + 12) % 12) + 1
        val gYear = calendar.get(Calendar.YEAR)

        // Chingam..Dhanu (months 1-5) fall Aug-Dec of the Gregorian year.
        // Makaram..Karkidakam (months 6-12) fall Jan-Aug of the following Gregorian year.
        val kollavarshamYear = if (mlMonthNum <= 5) gYear - 824 else gYear - 825

        return MalayalamDate(monthName, day, kollavarshamYear)
    }

    private fun rashiIndex(jd: Double): Int {
        val sidereal = siderealLongitude(jd)
        return floor(sidereal / 30.0).toInt().coerceIn(0, 11)
    }

    private fun siderealLongitude(jd: Double): Double {
        val tropical = sunApparentTropicalLongitude(jd)
        val ayanamsa = lahiriAyanamsa(jd)
        var sidereal = (tropical - ayanamsa) % 360.0
        if (sidereal < 0) sidereal += 360.0
        return sidereal
    }

    /** Low-precision solar position formula (accurate to ~0.01 degree). */
    private fun sunApparentTropicalLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0

        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val mDeg = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val m = Math.toRadians(mDeg)

        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
                (0.019993 - 0.000101 * t) * sin(2 * m) +
                0.000289 * sin(3 * m)

        var trueLongitude = (l0 + c) % 360.0
        if (trueLongitude < 0) trueLongitude += 360.0
        return trueLongitude
    }

    /** Linear approximation of the Lahiri ayanamsa (degrees), anchored at J2000. */
    private fun lahiriAyanamsa(jd: Double): Double {
        val yearsFromJ2000 = (jd - 2451545.0) / 365.25
        return 23.85 + 0.013969 * yearsFromJ2000
    }

    /** Julian Day Number, evaluated at local noon of the given calendar day for stability. */
    private fun julianDayAtLocalNoon(cal: Calendar): Double {
        val y0 = cal.get(Calendar.YEAR)
        val m0 = cal.get(Calendar.MONTH) + 1
        val d0 = cal.get(Calendar.DAY_OF_MONTH)

        var y = y0
        var m = m0
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4

        val jdMidnight = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d0 + b - 1524.5
        return jdMidnight + 0.5 // shift to local noon of that calendar day
    }
}
