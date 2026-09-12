export interface SuperAdminMenuItem {
  code: string;
  parentCode: string | null;
  title: {
    vi: string;
    en: string;
  };
  description: string;
  path: string;
  icon: string;
  badge?: string;
  children?: SuperAdminMenuItem[];
}

export const SUPERADMIN_MENU_TREE: SuperAdminMenuItem[] = [
  {
    code: 'SA_DASHBOARD',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Bảng Điều Khiển Tổng Quan',
      en: 'Platform Executive Dashboard',
    },
    description: 'Trung tâm trực quan hóa chỉ số vận hành, lưu lượng mạng, sức khỏe 17 microservices và tăng trưởng toàn sàn.',
    path: '/dashboard',
    icon: 'Activity',
    children: [
      {
        code: 'SA_DASH_METRICS',
        parentCode: 'SA_DASHBOARD',
        title: {
          vi: 'Lưu Lượng & Hiệu Năng Toàn Sàn',
          en: 'Traffic & Performance Metrics',
        },
        description: 'Theo dõi RPS thời gian thực, độ trễ API Gateway (P95/P99), tải CPU/RAM.',
        path: '/dashboard/metrics',
        icon: 'BarChart3',
      },
      {
        code: 'SA_DASH_HEALTH',
        parentCode: 'SA_DASHBOARD',
        title: {
          vi: 'Sức Khỏe 17 Microservices & DB',
          en: '17 Microservices & Infrastructure',
        },
        description: 'Giám sát kết nối cụm MySQL Galera, Redis Cluster, độ trễ Outbox.',
        path: '/dashboard/health',
        icon: 'HeartPulse',
      },
      {
        code: 'SA_DASH_REVENUE',
        parentCode: 'SA_DASHBOARD',
        title: {
          vi: 'Doanh Thu & Tăng Trưởng Tenant',
          en: 'Platform Revenue & Growth',
        },
        description: 'Thống kê doanh thu từ gói cước, phí hoa hồng giao dịch B2B.',
        path: '/dashboard/revenue',
        icon: 'DollarSign',
      },
    ],
  },
  {
    code: 'SA_TENANT_MGMT',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Quản Trị Doanh Nghiệp (Tenant)',
      en: 'Tenant & Quota Management',
    },
    description: 'Điều phối vòng đời, chính sách cấp phát tài nguyên và kiểm soát hạn ngạch dịch vụ đối tác doanh nghiệp.',
    path: '/tenants',
    icon: 'Building2',
    children: [
      {
        code: 'SA_TENANT_DIRECTORY',
        parentCode: 'SA_TENANT_MGMT',
        title: {
          vi: 'Danh Sách Đối Tác / Doanh Nghiệp',
          en: 'Tenant Directory & Lifecycle',
        },
        description: 'Tra cứu hồ sơ doanh nghiệp B2B, quản lý thông tin pháp nhân và hợp đồng.',
        path: '/tenants/directory',
        icon: 'Users',
      },
      {
        code: 'SA_TENANT_ONBOARDING',
        parentCode: 'SA_TENANT_MGMT',
        title: {
          vi: 'Thẩm Định & Kích Hoạt Tenant',
          en: 'Tenant Onboarding Workflow',
        },
        description: 'Quy trình phê duyệt thẩm định doanh nghiệp mới và cấp phát schema độc lập.',
        path: '/tenants/onboarding',
        icon: 'UserCheck',
      },
      {
        code: 'SA_TENANT_QUOTA',
        parentCode: 'SA_TENANT_MGMT',
        title: {
          vi: 'Hạn Ngạch & Gói Cước Quota',
          en: 'Tenant Quota & Tier Limits',
        },
        description: 'Cấu hình Max Users, Max Devices IoT và ngưỡng chặn Rate Limit.',
        path: '/tenants/quota',
        icon: 'Sliders',
      },
      {
        code: 'SA_TENANT_DECOMMISSION',
        parentCode: 'SA_TENANT_MGMT',
        title: {
          vi: 'Đóng Băng & Thu Hồi Dịch Vụ',
          en: 'Tenant Freeze & Data Purge',
        },
        description: 'Khóa dịch vụ khẩn cấp khi vi phạm hợp đồng, thực thi offboarding và xóa an toàn.',
        path: '/tenants/decommission',
        icon: 'ShieldAlert',
      },
    ],
  },
  {
    code: 'SA_MASTER_DATA',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Danh Mục Tham Số Gốc',
      en: 'Master Data Management',
    },
    description: 'Cơ sở dữ liệu danh mục dùng chung toàn sàn, làm quy chuẩn tham chiếu cho toàn bộ tenant.',
    path: '/master-data',
    icon: 'Database',
    children: [
      {
        code: 'SA_MD_GEOGRAPHY',
        parentCode: 'SA_MASTER_DATA',
        title: {
          vi: 'Địa Giới Hành Chính (63 Tỉnh/Thành)',
          en: 'Administrative Geography',
        },
        description: 'Quản lý dữ liệu phân cấp Tỉnh/Thành phố, Quận/Huyện phục vụ mở chi nhánh.',
        path: '/master-data/geography',
        icon: 'MapPin',
      },
      {
        code: 'SA_MD_BRANCH',
        parentCode: 'SA_MASTER_DATA',
        title: {
          vi: 'Mạng Lưới Chi Nhánh Toàn Quốc',
          en: 'Branch Directory & Network',
        },
        description: 'Danh bạ chi nhánh toàn hệ thống, mã định danh điểm giao dịch.',
        path: '/master-data/branches',
        icon: 'GitBranch',
      },
      {
        code: 'SA_MD_BANK_NETWORK',
        parentCode: 'SA_MASTER_DATA',
        title: {
          vi: 'Ngân Hàng, SWIFT/BIC & Ví Số',
          en: 'Bank Directory & SWIFT/BIC',
        },
        description: 'Danh sách liên minh NAPAS, mã quốc tế SWIFT/BIC và cổng thanh toán.',
        path: '/master-data/banks',
        icon: 'Landmark',
      },
      {
        code: 'SA_MD_CURRENCY_TAX',
        parentCode: 'SA_MASTER_DATA',
        title: {
          vi: 'Tiền Tệ ISO, Tỷ Giá FX & Thuế VAT',
          en: 'Currency, FX Rates & VAT',
        },
        description: 'Định nghĩa đơn vị tiền tệ ISO 4217, tỷ giá liên ngân hàng và biểu thuế VAT.',
        path: '/master-data/currencies',
        icon: 'Coins',
      },
    ],
  },
  {
    code: 'SA_MASTER_IAM',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Định Danh & Phân Quyền Gốc',
      en: 'Master IAM & Dynamic Menus',
    },
    description: 'Quản trị tài khoản đặc quyền Root, cây danh mục quyền hạn và thiết kế menu động.',
    path: '/master-iam',
    icon: 'KeyRound',
    children: [
      {
        code: 'SA_IAM_STAFF',
        parentCode: 'SA_MASTER_IAM',
        title: {
          vi: 'Tài Khoản Quản Trị Sàn (Root)',
          en: 'SuperAdmin Staff Management',
        },
        description: 'Cấp phát tài khoản Root, SuperAdmin Operators và phiên MFA.',
        path: '/master-iam/staff',
        icon: 'ShieldCheck',
      },
      {
        code: 'SA_IAM_PERMISSIONS',
        parentCode: 'SA_MASTER_IAM',
        title: {
          vi: 'Cây Quyền Hạn Hệ Thống',
          en: 'Master Permissions Repository',
        },
        description: 'Kho lưu trữ tập trung mã quyền API, quyền thực thi READ, WRITE, APPROVE.',
        path: '/master-iam/permissions',
        icon: 'Lock',
      },
      {
        code: 'SA_IAM_DYNAMIC_MENU',
        parentCode: 'SA_MASTER_IAM',
        title: {
          vi: 'Thiết Kế Cây Menu Động',
          en: 'Dynamic Menu Tree Designer',
        },
        description: 'Cấu hình cây menu đa cấp, gắn route frontend và quản lý i18n động.',
        path: '/master-iam/dynamic-menus',
        icon: 'FolderTree',
      },
      {
        code: 'SA_IAM_FEATURE_FLAGS',
        parentCode: 'SA_MASTER_IAM',
        title: {
          vi: 'Cờ Chức Năng (Feature Flags)',
          en: 'Global Feature Flags Manager',
        },
        description: 'Bật/tắt tức thời các module (IoT, AI, Gamification) quy mô toàn sàn.',
        path: '/master-iam/feature-flags',
        icon: 'ToggleRight',
      },
    ],
  },
  {
    code: 'SA_FIN_POLICIES',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Chính Sách Tài Chính Sàn',
      en: 'Global Financial Policies',
    },
    description: 'Quy chuẩn khung pháp lý số, quy tắc sinh lời tài chính và trần hạn mức giao dịch.',
    path: '/financial-policies',
    icon: 'BadgePercent',
    children: [
      {
        code: 'SA_FIN_LIMITS',
        parentCode: 'SA_FIN_POLICIES',
        title: {
          vi: 'Hạn Mức Giao Dịch Sàn/Trần',
          en: 'Floor & Ceiling Transaction Limits',
        },
        description: 'Cấu hình ngưỡng giao dịch tối thiểu/tối đa cho từng lệnh và luân chuyển ngày.',
        path: '/financial-policies/limits',
        icon: 'SlidersHorizontal',
      },
      {
        code: 'SA_FIN_FEE_RATES',
        parentCode: 'SA_FIN_POLICIES',
        title: {
          vi: 'Khung Biểu Phí Cơ Sở',
          en: 'Base Transaction Fee Framework',
        },
        description: 'Biểu phí tiêu chuẩn sàn cho chuyển tiền, rút tiền mặt vật lý, phí bảo trì IoT.',
        path: '/financial-policies/fees',
        icon: 'Receipt',
      },
      {
        code: 'SA_FIN_INTEREST_ENGINE',
        parentCode: 'SA_FIN_POLICIES',
        title: {
          vi: 'Động Cơ Tính Lãi Suất Tiết Kiệm',
          en: 'Savings Interest Engine',
        },
        description: 'Cấu hình công thức tính lãi tích lũy từng ngày, lãi suất bậc thang heo đất.',
        path: '/financial-policies/interest',
        icon: 'TrendingUp',
      },
      {
        code: 'SA_FIN_CALENDAR_CUTOFF',
        parentCode: 'SA_FIN_POLICIES',
        title: {
          vi: 'Lịch Làm Việc & Giờ Cut-Off',
          en: 'National Holidays & Cut-off Times',
        },
        description: 'Lịch quyết toán bù trừ liên ngân hàng và giờ Cut-off Time chốt sổ.',
        path: '/financial-policies/calendar',
        icon: 'CalendarDays',
      },
    ],
  },
  {
    code: 'SA_INTEGRATIONS',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Cổng Kết Nối Ngoại Vi',
      en: 'Third-Party Integrations',
    },
    description: 'Quản lý thông số kết nối OAuth2, Telco SMS, SMTP, eKYC và Hóa đơn điện tử.',
    path: '/integrations',
    icon: 'Network',
    children: [
      {
        code: 'SA_INT_SSO_OAUTH',
        parentCode: 'SA_INTEGRATIONS',
        title: {
          vi: 'Cổng Định Danh Tập Trung (OAuth2/SSO)',
          en: 'Identity Gateways (OAuth2/SSO)',
        },
        description: 'Cấu hình Google Identity, Apple ID, Enterprise Keycloak.',
        path: '/integrations/sso',
        icon: 'Fingerprint',
      },
      {
        code: 'SA_INT_TELCO_SMS',
        parentCode: 'SA_INTEGRATIONS',
        title: {
          vi: 'Hạ Tầng SMS Brandname & OTP',
          en: 'Telco SMS & OTP Routing',
        },
        description: 'Điều phối định tuyến SMS qua Viettel, VNPT, FPT Telecom.',
        path: '/integrations/sms',
        icon: 'MessageSquare',
      },
      {
        code: 'SA_INT_SMTP_GATEWAY',
        parentCode: 'SA_INTEGRATIONS',
        title: {
          vi: 'Hạ Tầng Thư Điện Tử (SMTP Gateway)',
          en: 'SMTP Master Gateway',
        },
        description: 'Quản lý máy chủ gửi mail thông báo giao dịch (SendGrid, AWS SES, Mailgun).',
        path: '/integrations/smtp',
        icon: 'Mail',
      },
      {
        code: 'SA_INT_EKYC_OCR',
        parentCode: 'SA_INTEGRATIONS',
        title: {
          vi: 'Cổng eKYC & OCR Hóa Đơn',
          en: 'eKYC & OCR Receipt Gateways',
        },
        description: 'Tích hợp xác thực CCCD gắn chip và API bóc tách dữ liệu biên lai mua sắm.',
        path: '/integrations/ekyc-ocr',
        icon: 'ScanLine',
      },
      {
        code: 'SA_INT_EINVOICE',
        parentCode: 'SA_INTEGRATIONS',
        title: {
          vi: 'Tích Hợp Hóa Đơn Điện Tử',
          en: 'E-Invoice API Integration',
        },
        description: 'Kết nối dịch vụ hóa đơn thuế điện tử (VNPT, Viettel-CA, MISA).',
        path: '/integrations/e-invoice',
        icon: 'FileSpreadsheet',
      },
    ],
  },
  {
    code: 'SA_SEC_AML',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Kiểm Soát Rủi Ro & AML',
      en: 'Platform Security & AML Defense',
    },
    description: 'Hệ thống phòng vệ an ninh mạng, chống rửa tiền tập trung và tự động ngắt mạch khẩn cấp.',
    path: '/security-aml',
    icon: 'Shield',
    children: [
      {
        code: 'SA_SEC_PASS_POLICY',
        parentCode: 'SA_SEC_AML',
        title: {
          vi: 'Chính Sách Mật Khẩu & 2FA',
          en: 'Security & Password Policy',
        },
        description: 'Quy định độ phức tạp mật khẩu, thời hạn đổi và quy tắc khóa tài khoản.',
        path: '/security-aml/password-policy',
        icon: 'Key',
      },
      {
        code: 'SA_SEC_TOKEN_LIFECYCLE',
        parentCode: 'SA_SEC_AML',
        title: {
          vi: 'Vòng Đời Token RS256 & Blacklist',
          en: 'JWT RS256 Token Lifecycle',
        },
        description: 'Cấu hình thời gian sống Token, điều phối khóa RS256 và thu hồi trên Redis.',
        path: '/security-aml/tokens',
        icon: 'Cpu',
      },
      {
        code: 'SA_SEC_GLOBAL_BLACKLIST',
        parentCode: 'SA_SEC_AML',
        title: {
          vi: 'Sổ Đen Toàn Sàn (Global Blacklist)',
          en: 'Platform Global Blacklist',
        },
        description: 'Chặn IP độc hại, số CCCD gian lận, địa chỉ MAC và số điện thoại lừa đảo.',
        path: '/security-aml/blacklist',
        icon: 'Ban',
      },
      {
        code: 'SA_SEC_ALERT_ROUTING',
        parentCode: 'SA_SEC_AML',
        title: {
          vi: 'Điều Phối Cảnh Báo Sự Cố',
          en: 'Incident Alert Routing',
        },
        description: 'Định tuyến cảnh báo theo cấp độ đẩy sang Webhook Slack, SMS On-Call.',
        path: '/security-aml/alerts',
        icon: 'BellRing',
      },
      {
        code: 'SA_SEC_CIRCUIT_BREAKER',
        parentCode: 'SA_SEC_AML',
        title: {
          vi: 'Ngắt Mạch Khẩn Cấp (Kill-Switch)',
          en: 'Circuit Breaker & Maintenance',
        },
        description: 'Công tắc ngắt mạch toàn diện (Global Kill-Switch) bảo vệ ví khi bị tấn công.',
        path: '/security-aml/circuit-breaker',
        icon: 'ZapOff',
      },
    ],
  },
  {
    code: 'SA_STORAGE_HARDWARE',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Kho Phần Cứng & Firmware',
      en: 'Storage & Hardware Repository',
    },
    description: 'Quản lý kho nhị phân S3/MinIO, tệp firmware OTA ESP32 và tham số viễn trắc thiết bị.',
    path: '/storage-hardware',
    icon: 'HardDrive',
    children: [
      {
        code: 'SA_STG_BINARY_BLOB',
        parentCode: 'SA_STORAGE_HARDWARE',
        title: {
          vi: 'Kho Lưu Trữ Tệp S3 / MinIO',
          en: 'Binary Object Storage (S3/MinIO)',
        },
        description: 'Phân vùng Bucket, danh mục MIME-Type và Pre-signed URL cho tài liệu KYC.',
        path: '/storage-hardware/s3-buckets',
        icon: 'Layers',
      },
      {
        code: 'SA_STG_FIRMWARE_OTA',
        parentCode: 'SA_STORAGE_HARDWARE',
        title: {
          vi: 'Quản Lý Firmware OTA (.bin)',
          en: 'OTA Firmware Releases & Checksum',
        },
        description: 'Lưu trữ tệp nhị phân .bin cho ESP32, quản lý phiên bản và tính toàn vẹn SHA256.',
        path: '/storage-hardware/firmware-ota',
        icon: 'FileCode',
      },
      {
        code: 'SA_STG_IOT_TELEMETRY',
        parentCode: 'SA_STORAGE_HARDWARE',
        title: {
          vi: 'Cấu Hình Viễn Trắc IoT',
          en: 'IoT Telemetry Baseline Config',
        },
        description: 'Thiết lập nhịp tim Heartbeat, ngưỡng ngắt gia tốc MPU6050 (>6G).',
        path: '/storage-hardware/telemetry',
        icon: 'Radio',
      },
    ],
  },
  {
    code: 'SA_AUDIT_COMPLIANCE',
    parentCode: 'PORTAL_SUPERADMIN',
    title: {
      vi: 'Pháp Lý, Kiểm Toán & Tuân Thủ',
      en: 'Platform Audit & Compliance',
    },
    description: 'Lưu vết bằng chứng pháp lý bất biến, quản trị phiên bản điều khoản và chính sách WORM.',
    path: '/audit-compliance',
    icon: 'FileCheck',
    children: [
      {
        code: 'SA_AUDIT_TERMS',
        parentCode: 'SA_AUDIT_COMPLIANCE',
        title: {
          vi: 'Văn Bản Pháp Lý & Điều Khoản',
          en: 'Legal Terms & Policy Versions',
        },
        description: 'Soạn thảo, cập nhật phiên bản điều khoản dịch vụ và chính sách bảo mật.',
        path: '/audit-compliance/terms',
        icon: 'FileText',
      },
      {
        code: 'SA_AUDIT_CONSENT_TRAIL',
        parentCode: 'SA_AUDIT_COMPLIANCE',
        title: {
          vi: 'Bằng Chứng Pháp Lý Khách Hàng',
          en: 'Customer Consent Audit Trail',
        },
        description: 'Lưu trữ Timestamp, IP, User-Agent và mã phiên bản điều khoản đã chấp thuận.',
        path: '/audit-compliance/consent-trail',
        icon: 'CheckCircle2',
      },
      {
        code: 'SA_AUDIT_IMMUTABLE_LOGS',
        parentCode: 'SA_AUDIT_COMPLIANCE',
        title: {
          vi: 'Nhật Ký Kiểm Toán Bất Biến (WORM)',
          en: 'Immutable WORM Audit Logs',
        },
        description: 'Nhật ký ghi một lần (WORM) ghi nhận mọi hành động nhạy cảm của SuperAdmin.',
        path: '/audit-compliance/immutable-logs',
        icon: 'History',
      },
      {
        code: 'SA_AUDIT_RETENTION_ARCHIVE',
        parentCode: 'SA_AUDIT_COMPLIANCE',
        title: {
          vi: 'Chính Sách Lưu Trữ & Cold Storage',
          en: 'Data Retention & Archiving',
        },
        description: 'Thiết lập chu kỳ dọn dẹp dữ liệu giao dịch và nén sang Cold Storage.',
        path: '/audit-compliance/retention',
        icon: 'Archive',
      },
    ],
  },
];
