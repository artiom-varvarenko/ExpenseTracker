import Joi from 'joi'

// Define expense schema using Joi
const expenseSchema = Joi.object({
    description: Joi.string()
        .min(3)
        .max(100)
        .required()
        .messages({
            'string.empty': 'Description is required',
            'string.min': 'Description must be at least 3 characters long',
            'string.max': 'Description cannot exceed 100 characters',
            'any.required': 'Description is required'
        }),

    amount: Joi.number()
        .positive()
        .precision(2)
        .required()
        .messages({
            'number.base': 'Amount must be a valid number',
            'number.positive': 'Amount must be positive',
            'any.required': 'Amount is required'
        }),

    date: Joi.date()
        .max('now')
        .required()
        .messages({
            'date.base': 'Please enter a valid date',
            'date.max': 'Date cannot be in the future',
            'any.required': 'Date is required'
        }),

    userId: Joi.number()
        .integer()
        .positive()
        .required()
        .messages({
            'number.base': 'Please select a user',
            'number.positive': 'Please select a valid user',
            'any.required': 'User is required'
        }),

    categoryIds: Joi.array()
        .items(Joi.number().integer().positive())
        .optional()
})

// Expense update schema (all fields optional)
const expenseUpdateSchema = Joi.object({
    description: Joi.string()
        .min(3)
        .max(100)
        .optional()
        .messages({
            'string.min': 'Description must be at least 3 characters long',
            'string.max': 'Description cannot exceed 100 characters'
        }),

    amount: Joi.number()
        .positive()
        .precision(2)
        .optional()
        .messages({
            'number.base': 'Amount must be a valid number',
            'number.positive': 'Amount must be positive'
        }),

    date: Joi.date()
        .max('now')
        .optional()
        .messages({
            'date.base': 'Please enter a valid date',
            'date.max': 'Date cannot be in the future'
        })
})

/**
 * Validate expense form data
 * @param {HTMLFormElement|Object} formOrData - Form element or data object
 * @returns {Promise<{isValid: boolean, errors: Object}>} Validation result
 */
export async function validateExpenseForm(formOrData) {
    let data

    if (formOrData instanceof HTMLFormElement) {
        const formData = new FormData(formOrData)
        data = {
            description: formData.get('description'),
            amount: parseFloat(formData.get('amount')),
            date: formData.get('date'),
            userId: parseInt(formData.get('userId'))
        }

        // Handle category checkboxes if present
        const categoryInputs = formOrData.querySelectorAll('input[name="categoryIds[]"]:checked')
        if (categoryInputs.length > 0) {
            data.categoryIds = Array.from(categoryInputs).map(input => parseInt(input.value))
        }
    } else {
        data = formOrData
    }

    try {
        await expenseSchema.validateAsync(data, { abortEarly: false })
        return { isValid: true, errors: {} }
    } catch (error) {
        if (error.isJoi) {
            const errors = {}
            error.details.forEach(detail => {
                errors[detail.path[0]] = detail.message
            })
            return { isValid: false, errors }
        }
        throw error
    }
}

/**
 * Validate expense update data
 * @param {Object} data - Update data
 * @returns {Promise<{isValid: boolean, errors: Object}>} Validation result
 */
export async function validateExpenseUpdate(data) {
    try {
        await expenseUpdateSchema.validateAsync(data, { abortEarly: false })
        return { isValid: true, errors: {} }
    } catch (error) {
        if (error.isJoi) {
            const errors = {}
            error.details.forEach(detail => {
                errors[detail.path[0]] = detail.message
            })
            return { isValid: false, errors }
        }
        throw error
    }
}

/**
 * Create custom validation rules for specific scenarios
 */
export const customValidations = {
    /**
     * Validate that expense amount doesn't exceed a monthly limit
     * @param {number} amount - Expense amount
     * @param {number} monthlyLimit - Monthly limit
     * @param {Array} existingExpenses - Existing expenses for the month
     * @returns {boolean|string} True if valid, error message if not
     */
    validateMonthlyLimit: (amount, monthlyLimit, existingExpenses) => {
        const totalExisting = existingExpenses.reduce((sum, expense) => sum + expense.amount, 0)
        const newTotal = totalExisting + amount

        if (newTotal > monthlyLimit) {
            const remaining = monthlyLimit - totalExisting
            return `This expense would exceed your monthly limit. You have $${remaining.toFixed(2)} remaining.`
        }

        return true
    },

    /**
     * Validate expense categories
     * @param {Array} categoryIds - Selected category IDs
     * @param {Array} validCategories - Valid categories
     * @returns {boolean|string} True if valid, error message if not
     */
    validateCategories: (categoryIds, validCategories) => {
        if (!categoryIds || categoryIds.length === 0) {
            return true // Categories are optional
        }

        const validIds = validCategories.map(cat => cat.id)
        const invalidIds = categoryIds.filter(id => !validIds.includes(id))

        if (invalidIds.length > 0) {
            return 'One or more selected categories are invalid'
        }

        return true
    },

    /**
     * Validate duplicate expense
     * @param {Object} expenseData - New expense data
     * @param {Array} existingExpenses - Existing expenses
     * @returns {boolean|string} True if valid, error message if not
     */
    validateDuplicate: (expenseData, existingExpenses) => {
        const duplicate = existingExpenses.find(expense =>
            expense.description === expenseData.description &&
            expense.amount === expenseData.amount &&
            expense.date === expenseData.date
        )

        if (duplicate) {
            return 'An expense with the same description, amount, and date already exists'
        }

        return true
    }
}

/**
 * Apply real-time validation to expense form
 * @param {HTMLFormElement} form - The form element
 */
export function applyExpenseFormValidation(form) {
    const inputs = {
        description: form.querySelector('[name="description"]'),
        amount: form.querySelector('[name="amount"]'),
        date: form.querySelector('[name="date"]'),
        userId: form.querySelector('[name="userId"]')
    }

    // Description validation
    if (inputs.description) {
        inputs.description.addEventListener('input', (e) => {
            const value = e.target.value.trim()
            clearError(e.target)

            if (value.length > 0 && value.length < 3) {
                showError(e.target, 'Description must be at least 3 characters long')
            } else if (value.length > 100) {
                showError(e.target, 'Description cannot exceed 100 characters')
            }
        })
    }

    // Amount validation
    if (inputs.amount) {
        inputs.amount.addEventListener('input', (e) => {
            const value = parseFloat(e.target.value)
            clearError(e.target)

            if (e.target.value && (isNaN(value) || value <= 0)) {
                showError(e.target, 'Amount must be a positive number')
            }
        })
    }

    // Date validation
    if (inputs.date) {
        inputs.date.addEventListener('change', (e) => {
            const selectedDate = new Date(e.target.value)
            const today = new Date()
            clearError(e.target)

            if (selectedDate > today) {
                showError(e.target, 'Date cannot be in the future')
            }
        })

        // Set max date to today
        inputs.date.max = new Date().toISOString().split('T')[0]
    }
}

/**
 * Show error message for a form field
 * @param {HTMLElement} field - The form field
 * @param {string} message - Error message
 */
function showError(field, message) {
    field.classList.add('is-invalid')

    let feedback = field.parentElement.querySelector('.invalid-feedback')
    if (!feedback) {
        feedback = document.createElement('div')
        feedback.className = 'invalid-feedback'
        field.parentElement.appendChild(feedback)
    }

    feedback.textContent = message
}

/**
 * Clear error message for a form field
 * @param {HTMLElement} field - The form field
 */
function clearError(field) {
    field.classList.remove('is-invalid')
    const feedback = field.parentElement.querySelector('.invalid-feedback')
    if (feedback) {
        feedback.remove()
    }
}