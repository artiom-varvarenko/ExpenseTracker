// Import styles
import '../scss/expenses.scss'

// Import dependencies
import axios from 'axios'
import { debounce, formatCurrency } from './modules/utils.js'
import { validateExpenseForm } from './modules/expenseValidation.js'
import { createExpenseChart } from './modules/charts.js'

// Expense API client using axios
const expenseApi = {
    async getAll() {
        const response = await axios.get('/api/expenses', {
            headers: { 'Accept': 'application/json' }
        })
        return response.data
    },

    async getById(id) {
        const response = await axios.get(`/api/expenses/${id}`, {
            headers: { 'Accept': 'application/json' }
        })
        return response.data
    },

    async getByUser(userId) {
        const response = await axios.get(`/api/expenses/user/${userId}`, {
            headers: { 'Accept': 'application/json' }
        })
        return response.data
    },

    async create(expenseData) {
        const response = await axios.post('/api/expenses', expenseData, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        return response.data
    },

    async update(id, patchData) {
        const response = await axios.patch(`/api/expenses/${id}`, patchData, {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        })
        return response.data
    },

    async delete(id) {
        await axios.delete(`/api/expenses/${id}`, {
            headers: { 'Accept': 'application/json' }
        })
    }
}

// Display expenses in a table with icons
function displayExpenses(expenses, containerId) {
    const container = document.getElementById(containerId)
    if (!container) return

    container.innerHTML = ''

    if (expenses.length === 0) {
        container.innerHTML = '<p><i class="bi bi-inbox"></i> No expenses found.</p>'
        return
    }

    const table = document.createElement('table')
    table.className = 'table table-hover'

    // Create header with icon
    const thead = document.createElement('thead')
    thead.innerHTML = `
        <tr>
            <th><i class="bi bi-hash"></i> ID</th>
            <th><i class="bi bi-card-text"></i> Description</th>
            <th><i class="bi bi-currency-dollar"></i> Amount</th>
            <th><i class="bi bi-calendar-date"></i> Date</th>
            <th><i class="bi bi-gear"></i> Actions</th>
        </tr>
    `
    table.appendChild(thead)

    // Create body
    const tbody = document.createElement('tbody')
    expenses.forEach(expense => {
        const tr = document.createElement('tr')
        tr.className = 'expense-item'

        // Add expense level class based on amount
        if (expense.amount > 100) {
            tr.classList.add('expense-high')
        } else if (expense.amount > 50) {
            tr.classList.add('expense-medium')
        } else {
            tr.classList.add('expense-low')
        }

        tr.innerHTML = `
            <td>${expense.id}</td>
            <td>${expense.description}</td>
            <td data-currency>${expense.amount}</td>
            <td>${new Date(expense.date).toLocaleDateString()}</td>
            <td>
                <button class="btn btn-sm btn-danger delete-expense" data-id="${expense.id}">
                    <i class="bi bi-trash"></i> Delete
                </button>
                <button class="btn btn-sm btn-info view-expense" data-id="${expense.id}">
                    <i class="bi bi-eye"></i> View
                </button>
                <button class="btn btn-sm btn-warning edit-expense" data-id="${expense.id}">
                    <i class="bi bi-pencil"></i> Edit
                </button>
            </td>
        `
        tbody.appendChild(tr)
    })
    table.appendChild(tbody)

    container.appendChild(table)

    // Format currency values
    container.querySelectorAll('[data-currency]').forEach(el => {
        const amount = parseFloat(el.textContent)
        if (!isNaN(amount)) {
            el.textContent = formatCurrency(amount)
        }
    })

    // Add event listeners
    setupEventListeners(containerId)
}

// Setup event listeners
function setupEventListeners(containerId) {
    // Delete expense
    document.querySelectorAll('.delete-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.closest('button').getAttribute('data-id')
            if (confirm('Are you sure you want to delete this expense?')) {
                try {
                    showLoadingSpinner(true)
                    await expenseApi.delete(id)
                    showNotification('Expense deleted successfully', 'success')
                    await loadAllExpenses()
                } catch (error) {
                    showNotification(`Failed to delete expense: ${error.message}`, 'danger')
                } finally {
                    showLoadingSpinner(false)
                }
            }
        })
    })

    // View expense
    document.querySelectorAll('.view-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.closest('button').getAttribute('data-id')
            try {
                showLoadingSpinner(true)
                const expense = await expenseApi.getById(id)
                displayExpenseDetails(expense)
            } catch (error) {
                showNotification(`Failed to load expense: ${error.message}`, 'danger')
            } finally {
                showLoadingSpinner(false)
            }
        })
    })

    // Edit expense
    document.querySelectorAll('.edit-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.closest('button').getAttribute('data-id')
            try {
                showLoadingSpinner(true)
                const expense = await expenseApi.getById(id)
                showEditForm(expense)
            } catch (error) {
                showNotification(`Failed to load expense: ${error.message}`, 'danger')
            } finally {
                showLoadingSpinner(false)
            }
        })
    })
}

// Display expense details
function displayExpenseDetails(expense) {
    const container = document.getElementById('expense-details')
    if (!container) return

    container.innerHTML = `
        <div class="card hover-card">
            <div class="card-header">
                <h4><i class="bi bi-receipt"></i> Expense Details</h4>
            </div>
            <div class="card-body">
                <p><strong><i class="bi bi-hash"></i> ID:</strong> ${expense.id}</p>
                <p><strong><i class="bi bi-card-text"></i> Description:</strong> ${expense.description}</p>
                <p><strong><i class="bi bi-currency-dollar"></i> Amount:</strong> ${formatCurrency(expense.amount)}</p>
                <p><strong><i class="bi bi-calendar-date"></i> Date:</strong> ${new Date(expense.date).toLocaleDateString()}</p>
                <p><strong><i class="bi bi-person"></i> User:</strong> ${expense.user?.name || 'Unknown'}</p>
                
                <h5><i class="bi bi-tags"></i> Categories:</h5>
                <ul>
                    ${expense.categories?.map(cat => `<li>${cat.name}</li>`).join('') || '<li>No categories</li>'}
                </ul>
            </div>
        </div>
    `
}

// Show loading spinner
function showLoadingSpinner(show) {
    const spinner = document.querySelector('.loading-spinner')
    if (spinner) {
        spinner.classList.toggle('active', show)
    }
}

// Show notification
function showNotification(message, type = 'info') {
    const container = document.getElementById('notifications')
    if (!container) return

    const alert = document.createElement('div')
    alert.className = `alert alert-${type} alert-dismissible fade show`
    alert.innerHTML = `
        <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-triangle'}"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `

    container.appendChild(alert)

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alert.remove()
    }, 5000)
}

// Load all expenses
async function loadAllExpenses() {
    try {
        showLoadingSpinner(true)
        const expenses = await expenseApi.getAll()
        displayExpenses(expenses, 'expenses-container')

        // Create chart if container exists
        const chartContainer = document.getElementById('expense-chart')
        if (chartContainer) {
            createExpenseChart(chartContainer, expenses)
        }
    } catch (error) {
        showNotification(`Error loading expenses: ${error.message}`, 'danger')
    } finally {
        showLoadingSpinner(false)
    }
}

// Initialize add expense form
function initializeAddExpenseForm() {
    const form = document.getElementById('add-expense-form')
    if (!form) return

    form.addEventListener('submit', async (e) => {
        e.preventDefault()

        // Validate form
        const validation = await validateExpenseForm(form)
        if (!validation.isValid) {
            showValidationErrors(validation.errors)
            return
        }

        const formData = new FormData(form)
        const expenseData = {
            description: formData.get('description'),
            amount: parseFloat(formData.get('amount')),
            date: formData.get('date'),
            userId: parseInt(formData.get('userId'))
        }

        try {
            showLoadingSpinner(true)
            await expenseApi.create(expenseData)
            showNotification('Expense added successfully!', 'success')
            form.reset()
            await loadAllExpenses()
        } catch (error) {
            showNotification(`Failed to add expense: ${error.message}`, 'danger')
        } finally {
            showLoadingSpinner(false)
        }
    })
}

// Show validation errors
function showValidationErrors(errors) {
    // Clear previous errors
    document.querySelectorAll('.form-validation-error').forEach(el => el.remove())

    Object.entries(errors).forEach(([field, message]) => {
        const input = document.querySelector(`[name="${field}"]`)
        if (input) {
            const errorDiv = document.createElement('div')
            errorDiv.className = 'form-validation-error'
            errorDiv.textContent = message
            input.parentElement.appendChild(errorDiv)
            input.classList.add('is-invalid')
        }
    })
}

// User expense search with debounce
const searchExpenses = debounce(async (userId) => {
    if (!userId) {
        document.getElementById('user-expenses-container').innerHTML = '<p>Please select a user</p>'
        return
    }

    try {
        showLoadingSpinner(true)
        const expenses = await expenseApi.getByUser(userId)
        displayExpenses(expenses, 'user-expenses-container')
    } catch (error) {
        showNotification(`Error loading user expenses: ${error.message}`, 'danger')
    } finally {
        showLoadingSpinner(false)
    }
}, 500)

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Add loading spinner to body
    if (!document.querySelector('.loading-spinner')) {
        const spinner = document.createElement('div')
        spinner.className = 'loading-spinner'
        spinner.innerHTML = '<div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>'
        document.body.appendChild(spinner)
    }

    // Add notifications container
    if (!document.getElementById('notifications')) {
        const notifications = document.createElement('div')
        notifications.id = 'notifications'
        notifications.style.position = 'fixed'
        notifications.style.top = '20px'
        notifications.style.right = '20px'
        notifications.style.zIndex = '9999'
        document.body.appendChild(notifications)
    }

    // Load expenses
    loadAllExpenses()

    // Initialize add expense form
    initializeAddExpenseForm()

    // User selector with debounce
    const userSelector = document.getElementById('user-selector')
    if (userSelector) {
        userSelector.addEventListener('change', (e) => {
            searchExpenses(e.target.value)
        })
    }

    // Add expense button for authenticated users
    const addButton = document.getElementById('add-expense-btn')
    if (addButton) {
        addButton.addEventListener('click', () => {
            const formContainer = document.getElementById('add-expense-container')
            if (formContainer) {
                formContainer.style.display = formContainer.style.display === 'none' ? 'block' : 'none'
            }
        })
    }
})