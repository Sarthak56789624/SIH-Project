import os
import sys
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    """
    Two-pass canvas that computes exact total pages and adds
    professional headers and footers to every page.
    """
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 752, "AI-ChainID Platform Study — Integrated Zero-Trust Architecture & Engineering Guide")
            self.drawRightString(558, 752, "Spring Boot :8080 | FastAPI :8000 | Web UI")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 744, 558, 744)
            
        # Footer (all pages)
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 32, footer_text)
        self.drawString(54, 32, "CONFIDENTIAL & COMPREHENSIVE STUDY REPORT — AI-CHAINID ENTERPRISE PLATFORM")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 44, 558, 44)
        self.restoreState()

def generate_pdf():
    # Write to both project directory and downloads for user convenience
    target_filename = "AI-ChainID_Integrated_Platform_Comprehensive_Guide.pdf"
    workspace_path = os.path.join(r"c:\Users\sonaw\Desktop\Testing\ai-chainid", target_filename)
    downloads_path = os.path.join(r"c:\Users\sonaw\Downloads", target_filename)
    
    # We will build to workspace_path and copy to downloads
    doc = SimpleDocTemplate(
        workspace_path,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom Brand Color Palette
    c_primary = colors.HexColor("#0B0F19")      # Deep Navy / Black
    c_accent_blue = colors.HexColor("#2563EB")  # Electric Blue
    c_indigo = colors.HexColor("#4F46E5")       # Primary Indigo
    c_violet = colors.HexColor("#6C5CE7")       # Brand Violet
    c_teal = colors.HexColor("#0D9488")         # Ledger Teal
    c_emerald = colors.HexColor("#059669")      # Success Green
    c_amber = colors.HexColor("#D97706")        # Warning Amber
    c_danger = colors.HexColor("#DC2626")       # Danger Red
    c_dark_text = colors.HexColor("#1E293B")    # Slate 800
    c_muted_text = colors.HexColor("#475569")   # Slate 600
    c_light_bg = colors.HexColor("#F8FAFC")     # Slate 50
    c_card_border = colors.HexColor("#E2E8F0")  # Slate 200

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        textColor=c_primary,
        spaceAfter=3
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubTitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=c_muted_text,
        spaceAfter=8
    )

    sec_header_style = ParagraphStyle(
        'SecHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=11,
        leading=15,
        textColor=c_indigo,
        spaceBefore=7,
        spaceAfter=3,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyMain',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.8,
        leading=11.2,
        textColor=c_dark_text,
        spaceAfter=4
    )

    bullet_style = ParagraphStyle(
        'BulletMain',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.8,
        leading=11,
        textColor=c_dark_text,
        leftIndent=8,
        firstLineIndent=-8,
        spaceAfter=2
    )

    callout_style = ParagraphStyle(
        'CalloutText',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=7.8,
        leading=11.2,
        textColor=colors.HexColor("#1E3A8A")
    )

    th_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=colors.white
    )

    tb_style = ParagraphStyle(
        'TableBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7,
        leading=9.2,
        textColor=c_dark_text
    )

    story = []

    # ==========================================
    # PAGE 1: TITLE, ARCHITECTURE & ROLE GOVERNANCE
    # ==========================================
    story.append(Paragraph("AI-ChainID: Integrated Platform Architectural Guide & Study Report", title_style))
    story.append(Paragraph("A Technical Deep-Dive on Role-Based Zero-Trust, Dual AI Models, Sequential Cryptographic Ledger, Hardware Custody & Automated Offboarding", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_indigo, spaceAfter=6))

    # Section 1: Executive Overview & Dual Engine Architecture
    story.append(Paragraph("1. Executive Summary & Full-Stack System Architecture", sec_header_style))
    story.append(Paragraph(
        "<b>AI-ChainID</b> unites continuous behavioral artificial intelligence with mathematical blockchain immutability to establish an uncompromising Zero-Trust Identity and Access Management (IAM) framework. "
        "Standard perimeter security assumes an authenticated identity is safe; AI-ChainID enforces the tenet that <i>'a valid identity never implies unconditional access.'</i> "
        "The platform seamlessly unifies three production-grade layers: a high-throughput Java 17 / Spring Boot core, an asynchronous Python FastAPI machine learning engine, and a dual-role enterprise Web console.",
        body_style
    ))

    arch_table_data = [
        [Paragraph("Subsystem Layer", th_style), Paragraph("Technology Stack", th_style), Paragraph("Core Functionalities & Zero-Trust Guarantees", th_style)],
        [
            Paragraph("<b>Security Core & Gateway</b>", tb_style),
            Paragraph("Java 17, Spring Boot 3.2, MySQL 8, JPA/Hibernate, JWT (:8080)", tb_style),
            Paragraph("Exposes REST APIs for User Directory, Off-Chain AES-256 PII Vault, Asset Lifecycle Engine, Cryptographic Blockchain Ledger, and Edge Verification Gate. Enforces rigorous RBAC security filters.", tb_style)
        ],
        [
            Paragraph("<b>Dual AI Intelligence Service</b>", tb_style),
            Paragraph("Python 3.10+, FastAPI, Scikit-Learn, Joblib (:8000)", tb_style),
            Paragraph("<b>Engine 1:</b> <code>RandomForestClassifier</code> recommending <code>ALLOW / REVIEW / DENY</code> for role least-privilege.<br/><b>Engine 2:</b> <code>RandomForestRegressor</code> + <code>IsolationForest</code> for live risk scoring & zero-day insider anomaly detection.", tb_style)
        ],
        [
            Paragraph("<b>Dual-Role Enterprise Client</b>", tb_style),
            Paragraph("HTML5, TailwindCSS, Canvas 2D, Lucide, Theme Engine", tb_style),
            Paragraph("Unified single-page client providing isolated <b>Administrator Portal</b> (Super Admin & Department Admins) and <b>Employee Portal</b> (DID Card, AI Access Requests, Asset Inventory, Verifiable Credentials).", tb_style)
        ]
    ]
    t_arch = Table(arch_table_data, colWidths=[95, 145, 264])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 4))

    # Section 2: Enterprise Authentication & Multi-Role Governance
    story.append(Paragraph("2. Enterprise Authentication, Dual-Role Login & Department Scoping", sec_header_style))
    story.append(Paragraph(
        "The integrated platform features a split-screen dark enterprise login gateway with interactive role switching, live canvas animations, and fine-grained department scoping:",
        body_style
    ))

    login_table_data = [
        [Paragraph("Feature Component", th_style), Paragraph("Technical Workflow & Implementation", th_style), Paragraph("Enterprise Security Value", th_style)],
        [
            Paragraph("<b>Role-Based Login Switcher</b>", tb_style),
            Paragraph("Instant toggle between <b>System Administrator</b> (CSO / Dept Lead) and <b>Employee Workspace</b>. Automatically routes users to their contextual workspace upon credential validation.", tb_style),
            Paragraph("Ensures strict separation of concerns. Regular staff cannot touch administrative governance tools, while admins can review scoped queues.", tb_style)
        ],
        [
            Paragraph("<b>Department Scoping & Isolation</b>", tb_style),
            Paragraph("Supports <b>Super Admin</b> (Global Scope: Engineering, Biomedical, Infrastructure) vs. <b>Dept Admins</b> (Vikram Mehta &bull; Engineering, Dr. Finch &bull; Biomedical, Karan Saxena &bull; Infrastructure).", tb_style),
            Paragraph("Dept leads are cryptographically isolated: they only view, approve, and assign assets for staff within their authorized organizational division.", tb_style)
        ],
        [
            Paragraph("<b>Self-Service Password Reset Modal</b>", tb_style),
            Paragraph("Employees submit password change requests directly from the login screen. Generates a pending verification item in the Admin Queue for approval.", tb_style),
            Paragraph("Prevents unauthorized account takeover. Admins inspect the request reason and approve the change, instantly anchoring it to the ledger.", tb_style)
        ]
    ]
    t_login = Table(login_table_data, colWidths=[95, 230, 179])
    t_login.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_indigo),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_login)

    story.append(PageBreak())

    # ==========================================
    # PAGE 2: ASSETS, REVOCATION & BLOCKCHAIN ENGINE
    # ==========================================
    story.append(Paragraph("3. Hardware Asset Custody & Cryptographic Instant Revocation", sec_header_style))
    story.append(Paragraph(
        "Hardware security tokens, biometric keycards, and encrypted developer laptops represent physical attack vectors. AI-ChainID couples digital identity with real-world hardware provenance.",
        body_style
    ))

    asset_table_data = [
        [Paragraph("Feature Area", th_style), Paragraph("Workflow & State Mechanics", th_style), Paragraph("Blockchain & Zero-Trust Impact", th_style)],
        [
            Paragraph("<b>Hardware Asset Lifecycle</b>", tb_style),
            Paragraph("Admins assign hardware tags (<code>AST-LAP-101</code> ThinkPad, <code>AST-CRD-201</code> RFID, <code>AST-TKN-301</code> YubiKey) via category tabs (Compute, Physical Access, Security Keys).", tb_style),
            Paragraph("<b>Immutable Hardware Provenance:</b> Assignment records are SHA-256 hashed and mined on-chain. Employees and admins have non-repudiable custody records.", tb_style)
        ],
        [
            Paragraph("<b>Instant Access Revocation</b>", tb_style),
            Paragraph("Admins trigger <code>POST /api/access-requests/{id}/revoke</code> with audited reason. Updates status from <code>APPROVED &rarr; REVOKED</code> and appends an <code>ACCESS_REVOKED</code> block.", tb_style),
            Paragraph("<b>Edge Kill Switch:</b> The perimeter gate (<code>verifyEdgeAccess</code>) verifies the absence of revocation blocks. Access is terminated across microservices in microseconds.", tb_style)
        ],
        [
            Paragraph("<b>Automated Offboarding & Cutoff</b>", tb_style),
            Paragraph("4-stage guided flow: selects employee &rarr; displays cryptographic footprint (DID, credentials, hardware) &rarr; user types confirmation &rarr; triggers disassembly animation.", tb_style),
            Paragraph("<b>Permanent Severance:</b> DID keypair is deactivated, vault credentials tombstoned, hardware flagged as <code>RETURN_PENDING</code>, and revocation block mined.", tb_style)
        ]
    ]
    t_asset = Table(asset_table_data, colWidths=[90, 210, 204])
    t_asset.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_teal),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_asset)
    story.append(Spacer(1, 4))

    # Section 4: Upgraded Sequential Blockchain & Break-Glass
    story.append(Paragraph("4. Upgraded Blockchain Engine: Sequential Chaining & Break-Glass Protocol", sec_header_style))
    
    bc_table_data = [
        [Paragraph("Architectural Dimension", th_style), Paragraph("Technical Implementation", th_style), Paragraph("Zero-Trust Verification Guarantee", th_style)],
        [
            Paragraph("<b>Cryptographic Block Chaining</b>", tb_style),
            Paragraph("Deterministic SHA-256 parent-child linking: <code>TxHash = SHA256(prevHash : entityType : entityId : blockNumber)</code> starting from Genesis <code>0x000...000</code>.", tb_style),
            Paragraph("Prevents history modification. Modifying any block byte invalidates all downstream transaction hashes in the chain.", tb_style)
        ],
        [
            Paragraph("<b>Chain Auditor (<code>/verify-ledger</code>)</b>", tb_style),
            Paragraph("Traverses entire ledger from Genesis to Block N, re-computing SHA-256 parent hashes and validating consensus state.", tb_style),
            Paragraph("Detects unauthorized direct SQL manipulation by rogue DBAs or compromised root accounts, flagging ledger corruption.", tb_style)
        ],
        [
            Paragraph("<b>Edge Gate (<code>/verify-edge-access</code>)</b>", tb_style),
            Paragraph("Zero-trust hardware simulator (GPU server, door lock). Queries blockchain consensus directly rather than trusting central SQL status.", tb_style),
            Paragraph("Denies access if SQL marks <code>APPROVED</code> but no cryptographically valid on-chain block exists (neutralizes SQL injections).", tb_style)
        ],
        [
            Paragraph("<b>Break-Glass Emergency Protocol</b>", tb_style),
            Paragraph("Admins bypass AI model blocks during critical outages with audited justification. Mines dedicated block with signature <code>EMERGENCY_OVERRIDE_ADMIN_1</code>.", tb_style),
            Paragraph("Guarantees emergency operational continuity while creating an indelible audit trail for forensic compliance audits.", tb_style)
        ]
    ]
    t_bc = Table(bc_table_data, colWidths=[95, 215, 194])
    t_bc.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_violet),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_bc)

    story.append(PageBreak())

    # ==========================================
    # PAGE 3: DUAL AI ENGINES & ROLE TRANSFER
    # ==========================================
    story.append(Paragraph("5. Dual AI Engines Deep-Dive: Permission Scoping & Risk Anomaly Detection", sec_header_style))
    story.append(Paragraph(
        "AI-ChainID's FastAPI microservice provides two specialized ML models that eliminate privilege creep and detect compromised accounts in real time:",
        body_style
    ))

    ai_table_data = [
        [Paragraph("AI Engine Component", th_style), Paragraph("Algorithmic Architecture & Training Data", th_style), Paragraph("Evaluation Metrics & Operational Behavior", th_style)],
        [
            Paragraph("<b>Permission Recommender<br/>(Feature 1)</b>", tb_style),
            Paragraph("<b>RandomForestClassifier:</b> Trained on 6,000 policy-grounded organizational rows across 7 enterprise resources. Features: Role, Department, Verified Credentials, Project Assignments.", tb_style),
            Paragraph("Outputs <code>ALLOW / REVIEW / DENY</code> with confidence score, human-readable rationale, and suggested TTL lease duration. Prevents toxic entitlement creep.", tb_style)
        ],
        [
            Paragraph("<b>Continuous Risk Scorer<br/>(Feature 2A)</b>", tb_style),
            Paragraph("<b>RandomForestRegressor:</b> Evaluates 15 continuous behavioral features: Hour of day, known MAC/device fingerprint, VPN subnet match, role-resource sensitivity, recent failed logins.", tb_style),
            Paragraph("Generates normalized risk score (0&ndash;100). Low risk (<30) enables seamless access; elevated risk (30&ndash;70) requires MFA; critical risk (>70) triggers policy block.", tb_style)
        ],
        [
            Paragraph("<b>Unsupervised Anomaly Detector<br/>(Feature 2B)</b>", tb_style),
            Paragraph("<b>IsolationForest:</b> Trained purely on normal (LOW-risk) enterprise traffic patterns to learn legitimate behavioral contours without synthetic bias.", tb_style),
            Paragraph("Detects zero-day insider threats and credential stuffing where attackers possess valid credentials but exhibit anomalous behavioral vectors.", tb_style)
        ]
    ]
    t_ai = Table(ai_table_data, colWidths=[95, 215, 194])
    t_ai.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_accent_blue),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_ai)
    story.append(Spacer(1, 4))

    # Section 6: Role Change & Scope Re-Analysis
    story.append(Paragraph("6. Departmental Role Change & Dynamic AI Scope Re-Analysis", sec_header_style))
    story.append(Paragraph(
        "When an employee transfers between departments (e.g. Software Developer moving to Lead Architect in Core Platform), static IAM systems retain legacy permissions. "
        "AI-ChainID executes a <b>Zero-Trust Scope Re-Mint</b> workflow:",
        body_style
    ))

    rc_table_data = [
        [Paragraph("Workflow Step", th_style), Paragraph("System Action & Visual Presentation", th_style), Paragraph("Zero-Trust Guarantee", th_style)],
        [
            Paragraph("<b>1. Target Specification</b>", tb_style),
            Paragraph("Admin selects employee profile, target role, and new department. Populates current DID and metadata.", tb_style),
            Paragraph("Enforces explicit administrative intent.", tb_style)
        ],
        [
            Paragraph("<b>2. AI Scope Synthesis</b>", tb_style),
            Paragraph("AI dynamically compares current permissions against target role scopes in real-time. Highlights scopes to Keep (Neutral), Revoke (Grey), and Grant (Indigo).", tb_style),
            Paragraph("Immediate visibility into privilege delta before any mutation occurs.", tb_style)
        ],
        [
            Paragraph("<b>3. Ledger Re-Minting</b>", tb_style),
            Paragraph("On execution, outdated scopes are severed, newly authorized scopes are minted, and a re-anchor transaction (<code>Ed25519-ScopeReMint</code>) is committed.", tb_style),
            Paragraph("Complete elimination of privilege accumulation over employee career lifecycles.", tb_style)
        ]
    ]
    t_rc = Table(rc_table_data, colWidths=[95, 235, 174])
    t_rc.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_rc)

    story.append(PageBreak())

    # ==========================================
    # PAGE 4: HACKATHON DEFENSE & API ENDPOINTS
    # ==========================================
    story.append(Paragraph("7. Hackathon Judge Defense & Counter-Argument Playbook", sec_header_style))
    story.append(Paragraph(
        "Preparation for technical evaluation: How to defend architectural design choices against common judge inquiries:",
        body_style
    ))

    defense_table_data = [
        [Paragraph("Judge Inquiry / Challenge", th_style), Paragraph("Typical Misconception", th_style), Paragraph("Winning Engineering Defense", th_style)],
        [
            Paragraph("<b>Why a Private Consortium Ledger instead of Public Ethereum?</b>", tb_style),
            Paragraph("<i>'Public blockchains offer greater decentralization.'</i>", tb_style),
            Paragraph("<b>GDPR & Enterprise Privacy:</b> Public chains leak corporate employee metadata and cannot honor GDPR 'Right to be Forgotten'. Our private consortium chain provides cryptographic immutability with microsecond finality and zero gas fees.", tb_style)
        ],
        [
            Paragraph("<b>Is the AI model dependent on synthetic training data?</b>", tb_style),
            Paragraph("<i>'Models trained on synthetic data fail in production.'</i>", tb_style),
            Paragraph("<b>Cold-Start Solution:</b> Synthetic policy grounding solves the Day-0 enterprise deployment problem. In live operation, our <b>Isolation Forest</b> is completely unsupervised and continuously retrains from real telemetry buffered in the monitoring feed.", tb_style)
        ],
        [
            Paragraph("<b>Why not auto-kill high-risk access tokens instantly?</b>", tb_style),
            Paragraph("<i>'High risk should immediately revoke access.'</i>", tb_style),
            Paragraph("<b>Operational Resilience (Anti-Self-DoS):</b> Hard auto-bans on 1-2% false positives crash hospital or banking operations. Following <b>NIST SP 800-207</b>, AI-ChainID flags elevated risk for Step-Up MFA and provides Break-Glass overrides.", tb_style)
        ]
    ]
    t_def = Table(defense_table_data, colWidths=[105, 125, 274])
    t_def.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_danger),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.5),
    ]))
    story.append(t_def)
    story.append(Spacer(1, 4))

    # Section 8: Core REST API Surface
    story.append(Paragraph("8. Core REST API Interface & Port Architecture Reference", sec_header_style))
    
    api_table_data = [
        [Paragraph("Endpoint & Method", th_style), Paragraph("Service & Port", th_style), Paragraph("Request Payload / Parameters", th_style), Paragraph("Response & Zero-Trust Output", th_style)],
        [
            Paragraph("<code>POST /ai/recommend-permissions</code>", tb_style),
            Paragraph("FastAPI (:8000)", tb_style),
            Paragraph("<code>role, dept, credentials[], resources[]</code>", tb_style),
            Paragraph("<code>ALLOW/DENY/REVIEW</code>, confidence score, suggested TTL lease.", tb_style)
        ],
        [
            Paragraph("<code>POST /ai/risk-score</code>", tb_style),
            Paragraph("FastAPI (:8000)", tb_style),
            Paragraph("<code>did, role, resource, hour, device, failed_attempts</code>", tb_style),
            Paragraph("<code>risk_score (0-100)</code>, anomaly flag, risk tier, risk factors.", tb_style)
        ],
        [
            Paragraph("<code>POST /api/access-requests/{id}/approve</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("<code>id, approvalNote</code>", tb_style),
            Paragraph("Status <code>APPROVED</code>, mines block, returns <code>txHash</code>.", tb_style)
        ],
        [
            Paragraph("<code>POST /api/access-requests/{id}/revoke</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("<code>id, revocationReason</code>", tb_style),
            Paragraph("Status <code>REVOKED</code>, appends cryptographic cutoff block.", tb_style)
        ],
        [
            Paragraph("<code>GET /api/blockchain/verify-ledger</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("None (Audit trigger)", tb_style),
            Paragraph("Sequential SHA-256 validation report across all blocks.", tb_style)
        ],
        [
            Paragraph("<code>POST /api/blockchain/verify-edge-access</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("<code>resourceId, userId</code>", tb_style),
            Paragraph("Perimeter gate check confirming active on-chain block.", tb_style)
        ]
    ]
    t_api = Table(api_table_data, colWidths=[120, 75, 140, 169])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_card_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2.2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2.2),
    ]))
    story.append(t_api)
    story.append(Spacer(1, 4))

    # Section 9: Elevator Pitch Box
    box_data = [[
        Paragraph(
            "<b>The Winning 30-Second Elevator Pitch:</b><br/>"
            "<i>'AI and Blockchain are opposing yet complementary forces in Zero-Trust security. AI provides the dynamic, contextual intelligence to evaluate behavioral anomalies and least-privilege scoping in real time. The Blockchain provides deterministic mathematical immutability, ensuring that even if a database administrator or root attacker manipulates access tables, the perimeter verification gate catches the cryptographic hash mismatch and stops unauthorized intrusion.'</i>",
            callout_style
        )
    ]]
    t_box = Table(box_data, colWidths=[504])
    t_box.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
        ('BOX', (0, 0), (-1, -1), 1, c_accent_blue),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(t_box)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated PDF at {workspace_path}")
    
    # Also write a copy to downloads if possible
    try:
        import shutil
        shutil.copyfile(workspace_path, downloads_path)
        print(f"SUCCESS: Copied PDF to {downloads_path}")
    except Exception as e:
        print(f"Notice: Could not copy to downloads ({e})")

if __name__ == "__main__":
    generate_pdf()
