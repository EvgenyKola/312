
//Подготовка страницы
let currentPath = window.location.pathname;

if (currentPath.includes("admin")) {
    document.addEventListener('DOMContentLoaded', () => {
        tableUpdate();
        activeTab();
        admin().then();
    });
} else if (currentPath.includes("user")) {
    document.addEventListener('DOMContentLoaded', async () => {
        await loadCurrentUser();
        activeTab();
    });
}

//Для страницы администрирования
async function admin() {

    //Редактирование. Форма
    const table = document.querySelector('table');

    table.addEventListener('click', function (event) {
        const target = event.target;

        if (target.classList.contains('editBtn')) {
            const row = target.closest('tr');
            const userId = row.children[0].textContent.trim();
            const username = row.children[1].textContent.trim();
            const lastName = row.children[2].textContent.trim();
            const age = row.children[3].textContent.trim();
            const roles = Array.from(row.children[4].querySelectorAll('.badge')) // Роли
                .map(roleElement => roleElement.textContent.trim());

            document.querySelector('#editUser input[name="id"]').value = userId;
            document.querySelector('#editUser input[name="name"]').value = username;
            document.querySelector('#editUser input[name="lastName"]').value = lastName;
            document.querySelector('#editUser input[name="age"]').value = age;
            document.querySelector('#role_admin_edit').checked = roles.includes('Администратор');
            document.querySelector('#role_user_edit').checked = roles.includes('Пользователь');

            const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
            modal.show();
        }
    });


    //Удаление. Форма
    table.addEventListener('click', function (event) {
        const target = event.target;


        if (target.classList.contains('deleteBtn')) {
            const row = target.closest('tr');
            const userId = row.children[0].textContent.trim();
            const username = row.children[1].textContent.trim();
            const lastName = row.children[2].textContent.trim();
            const age = row.children[3].textContent.trim();
            const roles = Array.from(row.children[4].querySelectorAll('.badge'))
                .map(roleElement => roleElement.textContent.trim());

            document.querySelector('#deleteUserForm input[name="id"]').value = userId;
            document.querySelector('#deleteUserForm input[readonly][name="name"]').value = username;
            document.querySelector('#deleteUserForm input[readonly][name="lastName"]').value = lastName;
            document.querySelector('#deleteUserForm input[readonly][name="age"]').value = age;

            const rolesContainer = document.querySelector('#deleteUserForm .actualRoles');
            rolesContainer.innerHTML = '';
            roles.forEach(role => {
                const roleSpan = document.createElement('span');
                roleSpan.textContent = role;
                roleSpan.classList.add('badge', role === 'Администратор' ? 'badge-danger' : 'badge-info');
                rolesContainer.appendChild(roleSpan);
            });

            const modal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
            modal.show();
        }
    });

    //Обновление. Запрос
    const editUserForm = document.getElementById('editUser');

    editUserForm.addEventListener('submit', async (event) => {
        event.preventDefault();


        const formData = new FormData(editUserForm);
        const userId = formData.get('id');
        const roles = [];

        if (document.getElementById('role_admin_edit').checked) {
            roles.push('ROLE_ADMIN');
        }
        if (document.getElementById('role_user_edit').checked) {
            roles.push('ROLE_USER');
        }

        const user = {
            id: userId,
            username: formData.get('name'),
            lastName: formData.get('lastName'),
            age: parseInt(formData.get('age'), 10),
            password: formData.get('password'),
            roles: roles.map(role => ({ name: role }))
        };

        try {
            const response = await fetch(`/admin/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(user)
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Пользователь обновлён', data);
                tableUpdate();
                closeModal();
            } else {
                console.error('Ошибка при обновлении пользователя', response.status);
            }
        } catch (error) {
            console.error('Ошибка сети', error);
        }
    });

    //Создание нового пользователяю Запрос
    document.getElementById('submitButton').addEventListener('click', async (event) => {
        event.preventDefault(); //

        const form = document.getElementById('addNewUser');
        const formData = new FormData(form);

        const params = new URLSearchParams();
        params.append('name', formData.get('name'));
        params.append('lastName', formData.get('lastName'));
        params.append('age', formData.get('age'));
        params.append('password', formData.get('password'));

        if (formData.getAll('roles').includes('ROLE_ADMIN')) {
            params.append('roles', 'ROLE_ADMIN');
        }
        if (formData.getAll('roles').includes('ROLE_USER')) {
            params.append('roles', 'ROLE_USER');
        }

        try {
            const response = await fetch('/admin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString(),
            });

            if (response.ok) {
                const result = await response.json();
                tableUpdate();
                console.log('Пользователь успешно создан:', result);
            } else {
                const errorData = await response.json();
                console.error('Ошибка при создании пользователя:', errorData.error || response.status);
            }
        } catch (error) {
            console.error('Ошибка при запросе:', error);
        }
    });

    //Удаление пользователя. Запрос
    document.getElementById('deleteUserForm').addEventListener('submit', async function (event) {
        event.preventDefault();

        const id = this.querySelector('input[name="id"]').value;

        try {
            const response = await fetch(`/admin/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Пользователь удален:', data);

                tableUpdate();
                closeModal();
            } else {
                console.error('Ошибка при удалении пользователя:', response.status);
            }
        } catch (error) {
            console.error('Ошибка:', error);
        }
    });

}

//Запрос даннных текущего пользователя
async function loadCurrentUser() {
    try {
        const response = await fetch('/user', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const data = await response.json();
        const currentUser = data.currentUser;

        document.getElementById('currentUserName').textContent = "Имя: " + currentUser.username;

        const rolesContainer = document.getElementById('currentUserRoles');
        rolesContainer.innerHTML = '';

        currentUser.roles.forEach(role => {
            const roleSpan = document.createElement('span');
            const roleText = role.name === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь';
            roleSpan.textContent = roleText;
            roleSpan.classList.add('badge', role.name === 'ROLE_ADMIN' ? 'badge-danger' : 'badge-info');
            rolesContainer.appendChild(roleSpan);
        });

        const tableBody = document.getElementById('usersTableBody');
        const userRow = document.createElement('tr');

        userRow.innerHTML = `
            <td>${currentUser.id}</td>
            <td>${currentUser.username}</td>
            <td>${currentUser.lastName}</td>
            <td>${currentUser.age}</td>
            <td></td>
        `;

        const rolesCell = userRow.querySelector('td:last-child');
        currentUser.roles.forEach(role => {
            const roleSpan = document.createElement('span');
            const roleText = role.name === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь';
            roleSpan.textContent = roleText;
            roleSpan.classList.add('badge', role.name === 'ROLE_ADMIN' ? 'badge-danger' : 'badge-info');
            rolesCell.appendChild(roleSpan);
            rolesCell.appendChild(document.createTextNode(' '));
        });

        //tableBody.innerHTML = '';
        tableBody.appendChild(userRow);

    } catch (error) {
        console.error('Ошибка при загрузке данных пользователя:', error);
        alert('Не удалось загрузить информацию о пользователе.');
    }
}

//Создание и обновление таблицы
function tableUpdate () {

    fetch('/admin')
        .then(response => response.json())
        .then(data => {
            const currentUser = data.currentUser;

            document.getElementById('currentUserName').textContent = `Имя: ${currentUser.username}`;
            const rolesContainer = document.getElementById('currentUserRoles');

            currentUser.roles.forEach(role => {
                const roleExists = Array.from(rolesContainer.children).some(child =>
                    child.textContent === (role.name === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь')
                );

                if (!roleExists) {
                    const roleSpan = document.createElement('span');
                    roleSpan.textContent = role.name === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь';
                    roleSpan.classList.add(role.name === 'ROLE_ADMIN' ? 'badge-danger' : 'badge-info', 'badge');
                    rolesContainer.appendChild(roleSpan);
                }
            });

            const usersTableBody = document.getElementById('usersTableBody');
            usersTableBody.innerHTML = '';
            data.users.forEach(user => {
                const row = document.createElement('tr');

                row.innerHTML = `
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.lastName}</td>
                    <td>${user.age}</td>
                    <td>
                        ${user.roles.map(role => `<span class="badge ${role.name === 'ROLE_ADMIN' ? 'badge-danger' : 'badge-info'}">${role.name === 'ROLE_ADMIN' ? 'Администратор' : 'Пользователь'}</span>`).join('')}
                    </td>
                    <td><button class="btn btn-info btn-sm editBtn" data-id="${user.id}">Редактировать</button></td>
                    <td><button class="btn btn-danger btn-sm deleteBtn" data-id="${user.id}">Удалить</button></td>
                `;

                usersTableBody.appendChild(row);
            });

            activateFirstTab();

        })
        .catch(error => {
            console.log('Ошибка при загрузке данных: '+ error);
        });

}

//Вкладки область работы администратора
function activateFirstTab() {

    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });

    document.querySelectorAll('.tab-pane ').forEach(tab => {
        tab.classList.remove('active', 'show');
    });

    const firstNavLink = document.querySelector('#usertable-tab');
    const firstTabPane = document.querySelector('.tab-pane');

    if (firstNavLink) {
        firstNavLink.classList.add('active');
    }

    if (firstTabPane) {
        firstTabPane.classList.add('active', 'show');
    }
}

//Вкладки Администрирование Пользователь
function activeTab() {
    const links = document.querySelectorAll('.nav-link');
    const currentPath = window.location.pathname;

    links.forEach(link => {
        const linkPath = new URL(link.getAttribute('href'), window.location.origin).pathname;

        if (linkPath === currentPath) {
            link.classList.add('activeTab');
        }
    });
}

//Закрытие модальных окон
function closeModal() {
    const editUserModalElement = document.getElementById('editUserModal');
    const editUserModal = bootstrap.Modal.getInstance(editUserModalElement);

    const deleteUserModalElement = document.getElementById('deleteUserModal');
    const deleteUserModal = bootstrap.Modal.getInstance(deleteUserModalElement);

    if (editUserModal) {
        editUserModal.hide();
    }

    if (deleteUserModal) {
        deleteUserModal.hide();
    }
}









