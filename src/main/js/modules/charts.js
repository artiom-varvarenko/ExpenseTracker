import Chart from 'chart.js/auto'
import _ from 'lodash'

// Store chart instances to prevent memory leaks
const chartInstances = new Map()

/**
 * Initialize charts on the page
 */
export function initializeCharts() {
    // Initialize any charts with data-chart attribute
    const chartContainers = document.querySelectorAll('[data-chart]')
    chartContainers.forEach(container => {
        const chartType = container.getAttribute('data-chart')
        const dataUrl = container.getAttribute('data-url')

        if (dataUrl) {
            // Fetch data and create chart
            fetch(dataUrl)
                .then(response => response.json())
                .then(data => createChart(container, chartType, data))
                .catch(error => console.error('Failed to load chart data:', error))
        }
    })
}

/**
 * Create an expense chart
 * @param {HTMLElement} container - Container element
 * @param {Array} expenses - Expense data
 * @returns {Chart} Chart instance
 */
export function createExpenseChart(container, expenses) {
    // Destroy existing chart if present
    destroyChart(container)

    // Group expenses by date
    const expensesByDate = _.groupBy(expenses, 'date')
    const dates = Object.keys(expensesByDate).sort()
    const amounts = dates.map(date =>
        expensesByDate[date].reduce((sum, expense) => sum + expense.amount, 0)
    )

    const ctx = container.getContext('2d')
    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates.map(date => formatChartDate(date)),
            datasets: [{
                label: 'Daily Expenses',
                data: amounts,
                borderColor: '#4a90e2',
                backgroundColor: 'rgba(74, 144, 226, 0.1)',
                borderWidth: 2,
                tension: 0.1,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Expense Trend',
                    font: {
                        size: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            return `Amount: $${context.parsed.y.toFixed(2)}`
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: (value) => `$${value}`
                    }
                }
            }
        }
    })

    // Store chart instance
    chartInstances.set(container, chart)
    return chart
}

/**
 * Create a category breakdown chart
 * @param {HTMLElement} container - Container element
 * @param {Array} expenses - Expense data
 * @returns {Chart} Chart instance
 */
export function createCategoryChart(container, expenses) {
    // Destroy existing chart if present
    destroyChart(container)

    // Group expenses by category
    const categoryTotals = {}
    expenses.forEach(expense => {
        if (expense.categories && expense.categories.length > 0) {
            expense.categories.forEach(category => {
                categoryTotals[category.name] = (categoryTotals[category.name] || 0) + expense.amount
            })
        } else {
            categoryTotals['Uncategorized'] = (categoryTotals['Uncategorized'] || 0) + expense.amount
        }
    })

    const ctx = container.getContext('2d')
    const chart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(categoryTotals),
            datasets: [{
                data: Object.values(categoryTotals),
                backgroundColor: [
                    '#4a90e2',
                    '#27ae60',
                    '#e74c3c',
                    '#f39c12',
                    '#9b59b6',
                    '#1abc9c',
                    '#34495e',
                    '#e67e22'
                ],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Expenses by Category',
                    font: {
                        size: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: (context) => {
                            const label = context.label || ''
                            const value = context.parsed || 0
                            const total = context.dataset.data.reduce((a, b) => a + b, 0)
                            const percentage = ((value / total) * 100).toFixed(1)
                            return `${label}: $${value.toFixed(2)} (${percentage}%)`
                        }
                    }
                },
                legend: {
                    position: 'bottom'
                }
            }
        }
    })

    // Store chart instance
    chartInstances.set(container, chart)
    return chart
}

/**
 * Create a monthly comparison chart
 * @param {HTMLElement} container - Container element
 * @param {Array} expenses - Expense data
 * @returns {Chart} Chart instance
 */
export function createMonthlyChart(container, expenses) {
    // Destroy existing chart if present
    destroyChart(container)

    // Group expenses by month
    const monthlyTotals = {}
    expenses.forEach(expense => {
        const date = new Date(expense.date)
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
        monthlyTotals[monthKey] = (monthlyTotals[monthKey] || 0) + expense.amount
    })

    // Sort months and get last 12 months
    const sortedMonths = Object.keys(monthlyTotals).sort().slice(-12)
    const amounts = sortedMonths.map(month => monthlyTotals[month])

    const ctx = container.getContext('2d')
    const chart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: sortedMonths.map(month => formatMonth(month)),
            datasets: [{
                label: 'Monthly Expenses',
                data: amounts,
                backgroundColor: '#4a90e2',
                borderColor: '#357abd',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Monthly Expense Comparison',
                    font: {
                        size: 16
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: (value) => `$${value}`
                    }
                }
            }
        }
    })

    // Store chart instance
    chartInstances.set(container, chart)
    return chart
}

/**
 * Create a generic chart
 * @param {HTMLElement} container - Container element
 * @param {string} type - Chart type
 * @param {Object} data - Chart data
 * @returns {Chart} Chart instance
 */
export function createChart(container, type, data) {
    // Destroy existing chart if present
    destroyChart(container)

    const ctx = container.getContext('2d')
    const chart = new Chart(ctx, {
        type: type,
        data: data.data,
        options: data.options || {
            responsive: true,
            maintainAspectRatio: false
        }
    })

    // Store chart instance
    chartInstances.set(container, chart)
    return chart
}

/**
 * Destroy a chart
 * @param {HTMLElement} container - Container element
 */
export function destroyChart(container) {
    const chart = chartInstances.get(container)
    if (chart) {
        chart.destroy()
        chartInstances.delete(container)
    }
}

/**
 * Update chart data
 * @param {HTMLElement} container - Container element
 * @param {Object} newData - New data
 */
export function updateChart(container, newData) {
    const chart = chartInstances.get(container)
    if (chart) {
        chart.data = newData
        chart.update()
    }
}

/**
 * Format date for chart display
 * @param {string} date - Date string
 * @returns {string} Formatted date
 */
function formatChartDate(date) {
    const d = new Date(date)
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

/**
 * Format month for display
 * @param {string} monthKey - Month key (YYYY-MM)
 * @returns {string} Formatted month
 */
function formatMonth(monthKey) {
    const [year, month] = monthKey.split('-')
    const date = new Date(year, parseInt(month) - 1)
    return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
}

// Export Chart for advanced usage
export { Chart }