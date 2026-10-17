# Malayalam Calendar Widget

A minimal 2×2 home screen widget for Android that shows:

- Malayalam month name (Malayalam script)
- Day of month (Arabic numeral)
- Kollavarsham era year (Arabic numeral)

No Gregorian date, no day-of-week, no Malayalam numerals, and tapping the widget does nothing.

## How the date is calculated

The Malayalam calendar is a **sidereal solar** calendar — a new month starts the moment the
Sun's sidereal longitude crosses a multiple of 30°. That's astronomically calculated here, not
looked up from a fixed table, using:

1. A standard low-precision solar position formula for the Sun's apparent tropical longitude.
2. A linear approximation of the Lahiri ayanamsa to convert to sidereal longitude.

This is accurate for everyday use, but being an approximation, it can occasionally be off by
a day right at a month boundary (Sankranti) compared to an official printed Panchangam. If you
ever spot a mismatch, it'll almost always be within a day of a month-start and self-corrects
the next day.

## Building and installing on your Phone

1. Install [Android Studio](https://developer.android.com/studio) if you don't have it.
2. Open this folder (`MalayalamCalendarWidget`) as a project in Android Studio — let it sync
   Gradle (it will auto-generate the Gradle wrapper jar on first sync).
3. Connect your Phone via USB with USB debugging enabled (Settings → About phone → tap
   "Build number" 7 times → Developer options → USB debugging).
4. Click **Run ▶** in Android Studio with your phone selected as the target. This installs the
   app (it has no launcher icon/screen — it only exists to host the widget).
5. On your phone: long-press the home screen → **Widgets** → find **Malayalam Calendar
   Widget** → drag it onto your home screen.

## Files of interest

- `MalayalamCalendarCalculator.kt` — the actual date math.
- `MalayalamCalendarWidgetProvider.kt` — renders the widget and refreshes it at midnight.
- `res/layout/widget_malayalam_calendar.xml` — the widget's visual layout.
- `res/xml/malayalam_calendar_widget_info.xml` — widget size/behavior metadata.

## Customizing

- Colors: `res/values/colors.xml`
- Text sizes / layout: `res/layout/widget_malayalam_calendar.xml`
