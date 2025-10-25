import Login from '../views/login/Index.vue';
import AdminDashboard from '../views/admin/Dashboard.vue';
import InspectorDashboard from '../views/inspector/Dashboard.vue';
import ViewerDashboard from '../views/viewer/Dashboard.vue';
import GraphIndex from '../views/graph/Index.vue';
import DeviceList from '../views/admin/device/List.vue';
import DeviceCategories from '../views/admin/device/Categories.vue';
import DeviceStatus from '../views/admin/device/Status.vue';
import TaskList from '../views/admin/task/List.vue';
import TaskAssign from '../views/admin/task/Assign.vue';
import TaskStats from '../views/admin/task/Stats.vue';
import AlertList from '../views/admin/alert/List.vue';
import AlertRules from '../views/admin/alert/Rules.vue';
import AlertStats from '../views/admin/alert/Stats.vue';
import UserList from '../views/admin/user/List.vue';
import UserRoles from '../views/admin/user/Roles.vue';
import Departments from '../views/admin/user/Departments.vue';

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'graph',
        name: 'AdminGraph',
        component: GraphIndex,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'devices',
        name: 'AdminDevices',
        component: DeviceList,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'devices/categories',
        name: 'AdminDeviceCategories',
        component: DeviceCategories,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'devices/status',
        name: 'AdminDeviceStatus',
        component: DeviceStatus,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'tasks',
        name: 'AdminTasks',
        component: TaskList,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'tasks/assign',
        name: 'AdminTaskAssign',
        component: TaskAssign,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'tasks/stats',
        name: 'AdminTaskStats',
        component: TaskStats,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      // 新增：告警管理子路由
      {
        path: 'alerts',
        name: 'AdminAlerts',
        component: AlertList,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: UserList,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'roles',
        name: 'AdminRoles',
        component: UserRoles,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'departments',
        name: 'AdminDepartments',
        component: Departments,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'alerts/rules',
        name: 'AdminAlertRules',
        component: AlertRules,
        meta: { requiresAuth: true, role: 'ADMIN' }
      },
      {
        path: 'alerts/stats',
        name: 'AdminAlertStats',
        component: AlertStats,
        meta: { requiresAuth: true, role: 'ADMIN' }
      }
    ]
  },
  {
    path: '/inspector',
    name: 'Inspector',
    component: InspectorDashboard,
    meta: { requiresAuth: true, role: 'INSPECTOR' }
  },
  {
    path: '/viewer',
    name: 'Viewer',
    component: ViewerDashboard,
    meta: { requiresAuth: true, role: 'VIEWER' }
  }
];

export default routes;