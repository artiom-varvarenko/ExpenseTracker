// Expense API client functions

// Get all expenses
async function getAllExpenses() {
    try {
        const response = await fetch('/api/expenses', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error fetching expenses: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Failed to fetch expenses:', error);
        return [];
    }
}

// Get expense by ID
async function getExpenseById(id) {
    try {
        const response = await fetch(`/api/expenses/${id}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Expense not found');
            }
            throw new Error(`Error fetching expense: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`Failed to fetch expense with ID ${id}:`, error);
        throw error;
    }
}

// Get expenses by user ID
async function getExpensesByUser(userId) {
    try {
        const response = await fetch(`/api/expenses/user/${userId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('User not found or has no expenses');
            }
            throw new Error(`Error fetching user expenses: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`Failed to fetch expenses for user ${userId}:`, error);
        return [];
    }
}

// Create a new expense
async function createExpense(expenseData) {
    try {
        const response = await fetch('/api/expenses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(expenseData)
        });
        
        if (!response.ok) {
            if (response.status === 400) {
                const errorData = await response.json();
                const errorMessage = Object.entries(errorData)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join(', ');
                throw new Error(`Validation error: ${errorMessage}`);
            }
            throw new Error(`Error creating expense: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Failed to create expense:', error);
        throw error;
    }
}

// Update an expense 
async function updateExpense(id, patchData) {
    try {
        const response = await fetch(`/api/expenses/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(patchData)
        });
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Expense not found');
            } else if (response.status === 400) {
                const errorData = await response.json();
                const errorMessage = Object.entries(errorData)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join(', ');
                throw new Error(`Validation error: ${errorMessage}`);
            }
            throw new Error(`Error updating expense: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`Failed to update expense with ID ${id}:`, error);
        throw error;
    }
}

// Delete an expense
async function deleteExpense(id) {
    try {
        const response = await fetch(`/api/expenses/${id}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Expense not found');
            }
            throw new Error(`Error deleting expense: ${response.status}`);
        }
        
        return true; // Successfully deleted
    } catch (error) {
        console.error(`Failed to delete expense with ID ${id}:`, error);
        throw error;
    }
}

// Example DOM manipulation functions

// Display expenses in a table
function displayExpenses(expenses, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = '';
    
    if (expenses.length === 0) {
        container.innerHTML = '<p>No expenses found.</p>';
        return;
    }
    
    const table = document.createElement('table');
    table.className = 'table table-striped';
    
    // Create header
    const thead = document.createElement('thead');
    thead.innerHTML = `
        <tr>
            <th>ID</th>
            <th>Description</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Actions</th>
        </tr>
    `;
    table.appendChild(thead);
    
    // Create body
    const tbody = document.createElement('tbody');
    expenses.forEach(expense => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${expense.id}</td>
            <td>${expense.description}</td>
            <td>${expense.amount}</td>
            <td>${expense.date}</td>
            <td>
                <button class="btn btn-sm btn-danger delete-expense" data-id="${expense.id}">Delete</button>
                <button class="btn btn-sm btn-info view-expense" data-id="${expense.id}">View</button>
                <button class="btn btn-sm btn-warning edit-expense" data-id="${expense.id}">Edit</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    
    container.appendChild(table);
    
    // Add event listeners for delete buttons
    document.querySelectorAll('.delete-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.getAttribute('data-id');
            if (confirm('Are you sure you want to delete this expense?')) {
                try {
                    await deleteExpense(id);
                    alert('Expense deleted successfully');
                    // Refresh the list
                    const expenses = await getAllExpenses();
                    displayExpenses(expenses, containerId);
                } catch (error) {
                    alert(`Failed to delete expense: ${error.message}`);
                }
            }
        });
    });
    
    // Add event listeners for view buttons
    document.querySelectorAll('.view-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.getAttribute('data-id');
            try {
                const expense = await getExpenseById(id);
                displayExpenseDetails(expense, 'expense-details');
            } catch (error) {
                alert(`Failed to retrieve expense details: ${error.message}`);
            }
        });
    });
    
    // Add event listeners for edit buttons
    document.querySelectorAll('.edit-expense').forEach(button => {
        button.addEventListener('click', async (e) => {
            const id = e.target.getAttribute('data-id');
            try {
                const expense = await getExpenseById(id);
                showEditForm(expense, containerId);
            } catch (error) {
                alert(`Failed to retrieve expense details: ${error.message}`);
            }
        });
    });
}

// Display expense details
function displayExpenseDetails(expense, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h4>Expense Details</h4>
            </div>
            <div class="card-body">
                <p><strong>ID:</strong> ${expense.id}</p>
                <p><strong>Description:</strong> ${expense.description}</p>
                <p><strong>Amount:</strong> ${expense.amount}</p>
                <p><strong>Date:</strong> ${expense.date}</p>
                <p><strong>User:</strong> ${expense.user ? expense.user.name : 'Unknown'}</p>
                
                <h5>Categories:</h5>
                <ul>
                    ${expense.categories && expense.categories.length > 0 
                      ? expense.categories.map(cat => `<li>${cat.name}</li>`).join('') 
                      : '<li>No categories</li>'}
                </ul>
            </div>
        </div>
    `;
}

// Show add expense form
function showAddForm(containerId) {
    const formContainer = document.getElementById('add-expense-container');
    if (!formContainer) return;
    
    formContainer.innerHTML = `
        <div class="card mb-4">
            <div class="card-header">
                <h4>Add New Expense</h4>
            </div>
            <div class="card-body">
                <form id="add-expense-form">
                    <div class="mb-3">
                        <label for="description" class="form-label">Description</label>
                        <input type="text" class="form-control" id="description" name="description" required minlength="3" maxlength="100">
                        <div class="invalid-feedback">Description must be between 3 and 100 characters.</div>
                    </div>
                    <div class="mb-3">
                        <label for="amount" class="form-label">Amount</label>
                        <input type="number" class="form-control" id="amount" name="amount" step="0.01" min="0.01" required>
                        <div class="invalid-feedback">Amount must be positive.</div>
                    </div>
                    <div class="mb-3">
                        <label for="date" class="form-label">Date</label>
                        <input type="date" class="form-control" id="date" name="date" required>
                        <div class="invalid-feedback">Date is required and cannot be in the future.</div>
                    </div>
                    <div class="mb-3">
                        <label for="user-id" class="form-label">User</label>
                        <select class="form-select" id="user-id" name="userId" required>
                            <option value="">-- Select User --</option>
                            <option value="1">User 1</option>
                            <option value="2">User 2</option>
                            <option value="3">User 3</option>
                        </select>
                        <div class="invalid-feedback">User is required.</div>
                    </div>
                    <button type="submit" class="btn btn-primary">Add Expense</button>
                </form>
            </div>
        </div>
    `;
    
    // Set max date to today
    const dateInput = document.getElementById('date');
    if (dateInput) {
        const today = new Date();
        dateInput.max = today.toISOString().split('T')[0];
        dateInput.value = today.toISOString().split('T')[0];
    }
    
    // Add form submit handler
    const form = document.getElementById('add-expense-form');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Form validation
            if (!form.checkValidity()) {
                e.stopPropagation();
                form.classList.add('was-validated');
                return;
            }
            
            const expenseData = {
                description: document.getElementById('description').value,
                amount: document.getElementById('amount').value,
                date: document.getElementById('date').value,
                userId: document.getElementById('user-id').value
            };
            
            try {
                await createExpense(expenseData);
                alert('Expense added successfully!');
                
                // Clear form
                form.reset();
                form.classList.remove('was-validated');
                
                // Refresh expenses list
                const expenses = await getAllExpenses();
                displayExpenses(expenses, containerId);
            } catch (error) {
                alert(`Failed to add expense: ${error.message}`);
            }
        });
    }
}

// Show edit expense form
function showEditForm(expense, containerId) {
    const container = document.getElementById('expense-details');
    if (!container) return;
    
    const formattedDate = expense.date ? new Date(expense.date).toISOString().split('T')[0] : '';
    
    container.innerHTML = `
        <div class="card">
            <div class="card-header">
                <h4>Edit Expense</h4>
            </div>
            <div class="card-body">
                <form id="edit-expense-form">
                    <input type="hidden" id="expense-id" value="${expense.id}">
                    <div class="mb-3">
                        <label for="edit-description" class="form-label">Description</label>
                        <input type="text" class="form-control" id="edit-description" name="description" value="${expense.description}" minlength="3" maxlength="100">
                        <div class="invalid-feedback">Description must be between 3 and 100 characters.</div>
                    </div>
                    <div class="mb-3">
                        <label for="edit-amount" class="form-label">Amount</label>
                        <input type="number" class="form-control" id="edit-amount" name="amount" step="0.01" min="0.01" value="${expense.amount}">
                        <div class="invalid-feedback">Amount must be positive.</div>
                    </div>
                    <div class="mb-3">
                        <label for="edit-date" class="form-label">Date</label>
                        <input type="date" class="form-control" id="edit-date" name="date" value="${formattedDate}">
                        <div class="invalid-feedback">Date cannot be in the future.</div>
                    </div>
                    <div class="d-flex justify-content-between">
                        <button type="submit" class="btn btn-primary">Save Changes</button>
                        <button type="button" class="btn btn-secondary" id="cancel-edit">Cancel</button>
                    </div>
                </form>
            </div>
        </div>
    `;
    
    // Set max date to today
    const dateInput = document.getElementById('edit-date');
    if (dateInput) {
        const today = new Date();
        dateInput.max = today.toISOString().split('T')[0];
    }
    
    // Track form changes for auto-save or enabling save button
    const form = document.getElementById('edit-expense-form');
    let formChanged = false;
    
    if (form) {
        // Add change listener to all form inputs
        form.querySelectorAll('input').forEach(input => {
            input.addEventListener('change', () => {
                formChanged = true;
            });
            input.addEventListener('input', () => {
                formChanged = true;
            });
        });
        
        // Form submit handler
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Form validation
            if (!form.checkValidity()) {
                e.stopPropagation();
                form.classList.add('was-validated');
                return;
            }
            
            const expenseId = document.getElementById('expense-id').value;
            const description = document.getElementById('edit-description').value;
            const amount = document.getElementById('edit-amount').value;
            const date = document.getElementById('edit-date').value;
            
            const patchData = {};
            
            if (description && description !== expense.description) {
                patchData.description = description;
            }
            
            if (amount && amount !== expense.amount) {
                patchData.amount = amount;
            }
            
            if (date && date !== formattedDate) {
                patchData.date = date;
            }
            
            // Only update if there are changes
            if (Object.keys(patchData).length > 0) {
                try {
                    const updatedExpense = await updateExpense(expenseId, patchData);
                    alert('Expense updated successfully!');
                    
                    // Display the updated expense
                    displayExpenseDetails(updatedExpense, 'expense-details');
                    
                    // Refresh expenses list
                    const expenses = await getAllExpenses();
                    displayExpenses(expenses, containerId);
                } catch (error) {
                    alert(`Failed to update expense: ${error.message}`);
                }
            } else {
                // If no changes were made, just revert back to the details view
                displayExpenseDetails(expense, 'expense-details');
            }
        });
        
        // Cancel edit button
        const cancelButton = document.getElementById('cancel-edit');
        if (cancelButton) {
            cancelButton.addEventListener('click', () => {
                displayExpenseDetails(expense, 'expense-details');
            });
        }
    }
}

// Load and display expenses when DOM is ready
document.addEventListener('DOMContentLoaded', async () => {
    const expensesContainer = document.getElementById('expenses-container');
    const userExpensesContainer = document.getElementById('user-expenses-container');
    const expenseDetailsContainer = document.getElementById('expense-details');
    
    // Add "Add New Expense" button above expenses container
    const addButtonContainer = document.createElement('div');
    addButtonContainer.className = 'mb-3';
    addButtonContainer.innerHTML = '<button class="btn btn-primary" id="show-add-form">Add New Expense</button>';
    
    if (expensesContainer && expensesContainer.parentNode) {
        expensesContainer.parentNode.insertBefore(addButtonContainer, expensesContainer);
    }
    
    // Add expense form container
    const addFormContainer = document.createElement('div');
    addFormContainer.id = 'add-expense-container';
    
    if (expensesContainer && expensesContainer.parentNode) {
        expensesContainer.parentNode.insertBefore(addFormContainer, expensesContainer);
    }
    
    // Show add form button click handler
    const showAddFormButton = document.getElementById('show-add-form');
    if (showAddFormButton) {
        showAddFormButton.addEventListener('click', () => {
            showAddForm('expenses-container');
        });
    }
    
    if (expensesContainer) {
        try {
            const expenses = await getAllExpenses();
            displayExpenses(expenses, 'expenses-container');
        } catch (error) {
            expensesContainer.innerHTML = `<p class="text-danger">Error loading expenses: ${error.message}</p>`;
        }
    }
    
    // If there's a user selector, set up the user expenses view
    const userSelector = document.getElementById('user-selector');
    if (userSelector && userExpensesContainer) {
        userSelector.addEventListener('change', async (e) => {
            const userId = e.target.value;
            if (userId) {
                try {
                    const expenses = await getExpensesByUser(userId);
                    displayExpenses(expenses, 'user-expenses-container');
                } catch (error) {
                    userExpensesContainer.innerHTML = `<p class="text-danger">Error loading user expenses: ${error.message}</p>`;
                }
            } else {
                userExpensesContainer.innerHTML = '<p>Please select a user</p>';
            }
        });
    }
}); 