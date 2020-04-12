package ua.turskyi.travelling.features.home.view.callback

import ua.turskyi.travelling.model.Country

interface OnVisitedCountryClickListener {
    fun onItemClick(country: Country)
    fun onItemLongClick(country: Country)
}