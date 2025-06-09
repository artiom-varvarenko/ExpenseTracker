// Import styles
import '../scss/site.scss'
import 'bootstrap'
import 'bootstrap-icons/font/bootstrap-icons.css'

// Import modules
import { initializeNavigation } from './modules/navigation.js'
import { initializeFormValidation } from './modules/formValidation.js'
import { initializeCharts } from './modules/charts.js'
import { formatCurrency, debounce } from './modules/utils.js'

// Initialize site-wide functionality
document.addEventListener('DOMContentLoaded', () => {
    // Initialize navigation
    initializeNavigation()

    // Initialize form validation on all forms
    initializeFormValidation()

    // Initialize tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach(el => new bootstrap.Tooltip(el))

    // Initialize popovers
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]')
    popoverTriggerList.forEach(el => new bootstrap.Popover(el))

    // Add currency formatting to all elements with data-currency attribute
    document.querySelectorAll('[data-currency]').forEach(el => {
        const amount = parseFloat(el.textContent)
        if (!isNaN(amount)) {
            el.textContent = formatCurrency(amount)
        }
    })

    console.log('Expense Tracker initialized')
})