import _ from 'lodash'

/**
 * Format a number as currency
 * @param {number} amount - The amount to format
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount)
}

/**
 * Create a debounced version of a function using lodash
 * @param {Function} func - The function to debounce
 * @param {number} wait - The debounce delay in milliseconds
 * @returns {Function} Debounced function
 */
export function debounce(func, wait) {
    return _.debounce(func, wait)
}

/**
 * Create a throttled version of a function using lodash
 * @param {Function} func - The function to throttle
 * @param {number} wait - The throttle delay in milliseconds
 * @returns {Function} Throttled function
 */
export function throttle(func, wait) {
    return _.throttle(func, wait)
}

/**
 * Deep clone an object using lodash
 * @param {Object} obj - The object to clone
 * @returns {Object} Cloned object
 */
export function deepClone(obj) {
    return _.cloneDeep(obj)
}

/**
 * Format a date to a readable string
 * @param {string|Date} date - The date to format
 * @returns {string} Formatted date string
 */
export function formatDate(date) {
    const d = new Date(date)
    return d.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
}

/**
 * Parse query parameters from URL
 * @returns {Object} Object containing query parameters
 */
export function getQueryParams() {
    const params = new URLSearchParams(window.location.search)
    const result = {}
    for (const [key, value] of params) {
        result[key] = value
    }
    return result
}

/**
 * Show or hide an element with animation
 * @param {HTMLElement} element - The element to show/hide
 * @param {boolean} show - Whether to show or hide
 */
export function toggleElement(element, show) {
    if (show) {
        element.style.display = 'block'
        element.classList.add('fade-in')
    } else {
        element.classList.remove('fade-in')
        setTimeout(() => {
            element.style.display = 'none'
        }, 300)
    }
}