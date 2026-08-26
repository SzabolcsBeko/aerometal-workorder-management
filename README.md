# Employee Component Right Manager

Full-stack example with Spring Boot + MySQL backend and React + Vite frontend.

## Model
- Employee(id, first_name, last_name, amp_number, hire_date)
- Component(id, name, description)
- AccessRight(id, name, description), persisted in `access_right` table
- WorkOrdertRegister(id, employee, component, right)
- Unique DB constraint: `(employee_id, component_id, access_right_id, workorder_number, workorder_date)`

## Run
1. Install Java 21, Maven, Node.js and MySQL.
2. MySQL credentials default to `root` / `root`; edit `backend/src/main/resources/application.yml` if needed.
3. Backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
5. Open http://localhost:5173

## Excel
The Overview tab has an `Export Excel` button. Endpoint: `GET /api/assignments/export`.

## API
- `/api/employees` GET/POST, `/api/employees/{id}` PUT/DELETE
- `/api/components` GET/POST, `/api/components/{id}` PUT/DELETE
- `/api/accessrights` GET/POST, `/api/accessrights/{id}` PUT/DELETE
- `/api/assignments` GET/POST, `/api/assignments/{id}` DELETE
- `/api/assignments/export` GET

## UI Test
- npm install
- npm run test:run

- npm run test:coverage

6. Class diagram
   <img width="539" height="392" alt="kép" src="https://github.com/user-attachments/assets/1212fa18-a613-40a2-b56b-ef7cea529a1c" />


