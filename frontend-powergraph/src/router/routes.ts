import Login from '../views/login/Index.vue';
import AdminDashboard from '../views/admin/Dashboard.vue';
import InspectorDashboard from '../views/inspector/Dashboard.vue';
import ViewerDashboard from '../views/viewer/Dashboard.vue';
import GraphIndex from '../views/graph/Index.vue';

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