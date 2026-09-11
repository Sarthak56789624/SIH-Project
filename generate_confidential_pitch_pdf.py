import os
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
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
        self.setFont("Helvetica-Bold", 7.5)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Running Header (pages > 1)
        if self._pageNumber > 1:
            self.drawString(54, 752, "AI-CHAINID: CONFIDENTIAL ENTERPRISE PITCH & FULL ARCHITECTURAL DOSSIER")
            self.drawRightString(558, 752, "STRICTLY CONFIDENTIAL")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(54, 744, 558, 744)
            
        # Running Footer (all pages)
        self.setFont("Helvetica", 7.5)
        footer_text = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 30, footer_text)
        self.drawString(54, 30, "CONFIDENTIAL & PROPRIETARY — PREPARED FOR EXECUTIVE EVALUATION & HACKATHON JURY")
        self.setStrokeColor(colors.HexColor("#CBD5E1"))
        self.setLineWidth(0.5)
        self.line(54, 42, 558, 42)
        self.restoreState()

def generate_pitch_dossier_pdf():
    target_path = r"c:\Users\sonaw\Desktop\Testing\ai-chainid\AI-ChainID_Confidential_Pitch_Architecture_Report.pdf"
    downloads_path = r"c:\Users\sonaw\Downloads\AI-ChainID_Confidential_Pitch_Architecture_Report.pdf"

    doc = SimpleDocTemplate(
        target_path,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    # Premium Corporate Color Palette
    c_primary = colors.HexColor("#0B0F19")      # Dark Navy
    c_indigo = colors.HexColor("#4F46E5")       # Primary Indigo
    c_blue = colors.HexColor("#2563EB")         # Electric Blue
    c_teal = colors.HexColor("#0D9488")         # Ledger Teal
    c_emerald = colors.HexColor("#059669")      # Green
    c_amber = colors.HexColor("#D97706")        # Warning Amber
    c_danger = colors.HexColor("#DC2626")       # Red
    c_dark_text = colors.HexColor("#1E293B")    # Slate 800
    c_muted = colors.HexColor("#475569")        # Slate 600
    c_light_bg = colors.HexColor("#F8FAFC")     # Slate 50
    c_border = colors.HexColor("#E2E8F0")       # Border

    title_style = ParagraphStyle(
        'DocTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=16, leading=20, textColor=c_primary, spaceAfter=2
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubTitle', parent=styles['Normal'],
        fontName='Helvetica', fontSize=8.5, leading=12, textColor=c_muted, spaceAfter=5
    )

    sec_header_style = ParagraphStyle(
        'SecHeader', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=10, leading=13.5, textColor=c_indigo, spaceBefore=5, spaceAfter=2, keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyMain', parent=styles['Normal'],
        fontName='Helvetica', fontSize=7.4, leading=10.4, textColor=c_dark_text, spaceAfter=3
    )

    callout_style = ParagraphStyle(
        'CalloutText', parent=styles['Normal'],
        fontName='Helvetica-Oblique', fontSize=7.2, leading=10.2, textColor=colors.HexColor("#1E3A8A")
    )

    th_style = ParagraphStyle(
        'TableHeader', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=7, leading=9, textColor=colors.white
    )

    tb_style = ParagraphStyle(
        'TableBody', parent=styles['Normal'],
        fontName='Helvetica', fontSize=6.7, leading=8.6, textColor=c_dark_text
    )

    story = []

    # ==========================================
    # PAGE 1: EXECUTIVE PITCH, PROBLEM & ARCHITECTURE
    # ==========================================
    story.append(Paragraph("AI-ChainID: Confidential Technical Pitch & Comprehensive System Dossier", title_style))
    story.append(Paragraph("A Complete Deep-Dive on Dual AI Intelligence, 3-Key Quorum, Cryptographic Ledger, Asset Custody & Zero-Trust Defense", subtitle_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_indigo, spaceAfter=4))

    # Executive Pitch Box
    pitch_box = [[
        Paragraph(
            "<b>THE WINNING 60-SECOND EXECUTIVE PITCH:</b><br/>"
            "<i>'Judges, modern cybersecurity is broken because it assumes that once an identity is authenticated, it can be trusted. In conventional IAM (Okta, Active Directory, AWS), if a rogue DBA or hacker compromises root SQL credentials, they can silently grant permissions with zero audit trail.<br/>"
            "<b>AI-ChainID enforces a radically different doctrine: A valid identity never implies unconditional access, and SQL is merely a temporary operational cache — the Blockchain Ledger is the single source of cryptographic truth.</b><br/>"
            "We couple two opposing forces: <b>Dual AI Models</b> (Scikit-Learn) that evaluate contextual behavioral risk in real-time, and an <b>Enterprise Cryptographic Consortium Ledger</b> (SHA-256 Merkle chained) that guarantees deterministic mathematical immutability. Even if an attacker steals the Super Admin password, our <b>3-Key Consensus Quorum</b> and <b>Zero-Trust Edge Gate</b> catch the anomaly and shut the door at the perimeter.'</i>",
            callout_style
        )
    ]]
    t_pitch = Table(pitch_box, colWidths=[504])
    t_pitch.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#EFF6FF")),
        ('BOX', (0, 0), (-1, -1), 1, c_blue),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(t_pitch)
    story.append(Spacer(1, 3))

    # Section 1: Full-Stack Architecture
    story.append(Paragraph("1. Full-Stack System Architecture & Runtime Topology", sec_header_style))
    story.append(Paragraph(
        "AI-ChainID is engineered as an asynchronous, distributed microservice ecosystem that operates seamlessly alongside existing enterprise identity providers (Okta, Azure AD, PingIdentity):",
        body_style
    ))

    arch_data = [
        [Paragraph("Subsystem Layer", th_style), Paragraph("Runtime & Ports", th_style), Paragraph("Core Technical Responsibilities & Zero-Trust Capabilities", th_style)],
        [
            Paragraph("<b>Security Core & Gateway</b>", tb_style),
            Paragraph("Java 17, Spring Boot 3.2, MySQL 8, Jakarta EE (:8080)", tb_style),
            Paragraph("Orchestrates REST API endpoints, JWT security, RBAC access control filters, off-chain AES-256-GCM PII encryption, hardware asset custody state machines, and SHA-256 blockchain ledger mining.", tb_style)
        ],
        [
            Paragraph("<b>Dual AI Intelligence Microservice</b>", tb_style),
            Paragraph("Python 3.10+, FastAPI, Scikit-Learn, Joblib (:8000)", tb_style),
            Paragraph("<b>Engine 1:</b> <code>RandomForestClassifier</code> recommending least-privilege scopes.<br/><b>Engine 2A:</b> <code>RandomForestRegressor</code> scoring continuous risk (0–100).<br/><b>Engine 2B:</b> Unsupervised <code>IsolationForest</code> identifying novel zero-day insider anomalies.", tb_style)
        ],
        [
            Paragraph("<b>Enterprise Dual-Role Web Console</b>", tb_style),
            Paragraph("HTML5, TailwindCSS, Canvas 2D Mesh, Lucide Icons", tb_style),
            Paragraph("Single-page reactive client with strict role switching: (1) <b>Admin Portal</b> with executive dashboards, 10-step lifecycle map, and approval queues; (2) <b>Employee Portal</b> with DID card, verifiable credentials, and hardware asset receipts.", tb_style)
        ]
    ]
    t_arch = Table(arch_data, colWidths=[95, 135, 274])
    t_arch.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_arch)
    story.append(Spacer(1, 3))

    # Section 2: Decentralized Identity & Privacy
    story.append(Paragraph("2. W3C Decentralized Identity (DID) & Dual-Storage Privacy (GDPR/HIPAA)", sec_header_style))
    story.append(Paragraph(
        "<b>W3C DID Implementation:</b> Identifiers follow <code>did:org:&lt;id&gt;:&lt;username&gt;</code> paired with asymmetric **Ed25519** public/private keypairs, replacing phishable passwords. "
        "<b>The GDPR Solution:</b> Public blockchains violate GDPR Article 17 ('Right to be Forgotten'). AI-ChainID solves this via **Dual-Storage Separation**: "
        "Personal Identifiable Information (PII) is encrypted with **AES-256-GCM** off-chain. Only pseudonymous DIDs and cryptographic commitment hashes (<code>SHA256(encrypted_data)</code>) are anchored on-chain. "
        "When an employee departs, the off-chain key and PII are destroyed, achieving 100% GDPR compliance while preserving tamper-evident ledger proofs.",
        body_style
    ))

    story.append(PageBreak())

    # ==========================================
    # PAGE 2: DUAL AI ENGINES & 3-KEY QUORUM
    # ==========================================
    story.append(Paragraph("3. Deep-Dive: Dual AI Engines (Scikit-Learn Microservice)", sec_header_style))
    story.append(Paragraph(
        "The Python FastAPI microservice (:8000) operates two specialized, lightweight ML engines delivering sub-15ms inference latency without creating gateway bottlenecks:",
        body_style
    ))

    ai_data = [
        [Paragraph("AI Component", th_style), Paragraph("Underlying Model & Features", th_style), Paragraph("Zero-Trust Function & Enterprise Benefit", th_style)],
        [
            Paragraph("<b>Engine 1: Permission Recommender</b><br/><i>'What access should this person have?'</i>", tb_style),
            Paragraph("<b>RandomForestClassifier:</b> Trained on 6,000 policy-grounded organizational rows across 7 enterprise resources. Features: Role, Department, Projects, Verified Credentials.", tb_style),
            Paragraph("Outputs <code>ALLOW / REVIEW / DENY</code>, confidence percentage, human-readable rationale, and suggested TTL lease duration. Prevents toxic privilege accumulation from Day 1.", tb_style)
        ],
        [
            Paragraph("<b>Engine 2A: Continuous Risk Scorer</b><br/><i>'Is this request context normal?'</i>", tb_style),
            Paragraph("<b>RandomForestRegressor:</b> Evaluates 15 continuous behavioral features: Hour of day, known device MAC, corporate VPN subnet, failed attempts, and request velocity.", tb_style),
            Paragraph("Generates normalized risk score (0–100). Low risk (<30) allows seamless workflow; elevated risk (30–70) triggers Step-Up MFA; critical risk (>70) triggers an autonomous policy block.", tb_style)
        ],
        [
            Paragraph("<b>Engine 2B: Unsupervised Anomaly Detector</b><br/><i>'Is this a zero-day insider threat?'</i>", tb_style),
            Paragraph("<b>IsolationForest:</b> Trained purely on normal (LOW-risk) traffic patterns to isolate multivariate outlier vectors without synthetic bias.", tb_style),
            Paragraph("Catches compromised credentials where an attacker possesses a valid password but exhibits abnormal behavioral contours (e.g. 2:30 AM access from unusual endpoints).", tb_style)
        ]
    ]
    t_ai = Table(ai_data, colWidths=[110, 185, 209])
    t_ai.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_indigo),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_ai)
    story.append(Spacer(1, 3))

    # Section 4: The 3-Key Defense Quorum
    story.append(Paragraph("4. The 3-Key Defense Quorum (Preventing Compromised Admin Takeovers)", sec_header_style))
    story.append(Paragraph(
        "<b>The Threat:</b> What if an attacker steals the Super Admin's email and password? In ordinary IAM, the attacker has unchecked power. In AI-ChainID, access requires a **3-Key Consensus Quorum**:",
        body_style
    ))

    quorum_data = [
        [Paragraph("Consensus Key", th_style), Paragraph("Authority & Evaluation Mechanism", th_style), Paragraph("Security Guarantee Against Stolen Credentials", th_style)],
        [
            Paragraph("<b>Key 1: AI Behavioral Risk Key</b>", tb_style),
            Paragraph("Autonomous Scikit-Learn evaluation of 15 live telemetry signals.", tb_style),
            Paragraph("If the request originates from an unrecognized IP, device, or abnormal hour, AI triggers a <b>POLICY BLOCK</b>. A stolen password cannot fake legitimate telemetry.", tb_style)
        ],
        [
            Paragraph("<b>Key 2: Central Admin Governance Key</b>", tb_style),
            Paragraph("Central administrative sign-off or Break-Glass emergency override.", tb_style),
            Paragraph("Overriding an AI block requires invoking <b>NIST SP 800-207 Break-Glass</b>, permanently recording signature <code>EMERGENCY_OVERRIDE_ADMIN_1</code> on-chain.", tb_style)
        ],
        [
            Paragraph("<b>Key 3: Sub-Admin / Dept Lead Key</b>", tb_style),
            Paragraph("Department-scoped authorization (Engineering, Biomedical, Infrastructure).", tb_style),
            Paragraph("Fine-grained multi-tenancy prevents a single compromised account from laterally elevating privileges across other enterprise divisions.", tb_style)
        ]
    ]
    t_quorum = Table(quorum_data, colWidths=[105, 175, 224])
    t_quorum.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_teal),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_quorum)

    story.append(PageBreak())

    # ==========================================
    # PAGE 3: BLOCKCHAIN LEDGER, ASSETS & WORKFLOWS
    # ==========================================
    story.append(Paragraph("5. Cryptographic Blockchain Ledger & Edge Resource Gate", sec_header_style))
    story.append(Paragraph(
        "AI-ChainID maintains an immutable private consortium ledger with sequential parent-child SHA-256 block linking: "
        "<code>TxHash = SHA256(prevHash : entityType : entityId : blockNumber : timestamp)</code>. "
        "Block #1 (Genesis) anchors the trust root. Every subsequent event seals the previous hash:",
        body_style
    ))

    ledger_features = [
        [Paragraph("Blockchain Subsystem", th_style), Paragraph("Technical Execution & API Endpoint", th_style), Paragraph("Enterprise Defense Impact", th_style)],
        [
            Paragraph("<b>Zero-Trust Edge Gate Simulator</b>", tb_style),
            Paragraph("<code>POST /api/blockchain/verify-edge-access</code><br/>Simulates physical NFC doors, GPU servers, and SSH bastions.", tb_style),
            Paragraph("<b>Stops Direct SQL Injection:</b> Perimeter devices verify on-chain blocks directly. If an attacker updates MySQL to <code>status='APPROVED'</code> without an on-chain block, access is denied.", tb_style)
        ],
        [
            Paragraph("<b>Active Chain Integrity Auditor</b>", tb_style),
            Paragraph("<code>GET /api/blockchain/verify-ledger</code><br/>Traverses all blocks from Genesis to Block N re-calculating hashes.", tb_style),
            Paragraph("<b>Catches Rogue DBAs:</b> Instantly flags any modified byte or deleted record as a cryptographic ledger breach.", tb_style)
        ],
        [
            Paragraph("<b>Break-Glass Emergency Protocol</b>", tb_style),
            Paragraph("<code>POST /api/access-requests/{id}/break-glass</code><br/>Emergency AI policy override with mandatory justification.", tb_style),
            Paragraph("<b>Prevents Operational Self-DoS:</b> Keeps critical hospital/banking systems running during outages while creating non-repudiable audit proofs.", tb_style)
        ]
    ]
    t_lf = Table(ledger_features, colWidths=[110, 185, 209])
    t_lf.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_lf)
    story.append(Spacer(1, 3))

    # Section 6: Dynamic Zero-Trust Lifecycle Workflows
    story.append(Paragraph("6. Dynamic Zero-Trust Lifecycle Workflows", sec_header_style))
    story.append(Paragraph(
        "<b>A. Hardware Asset Custody:</b> Manages physical security hardware (ThinkPads, RFID Badges, YubiKey 5 NFC) through finite states (<code>AVAILABLE &rarr; ASSIGNED &rarr; RETURNED &rarr; RETIRED</code>). "
        "Double-assignment is barred at the database level, and every custody transfer is SHA-256 hashed and mined on-chain, establishing complete non-repudiation.<br/>"
        "<b>B. Role Change & Dynamic Scope Re-Analysis:</b> When an employee transfers departments (e.g. Developer moving to Lead Architect), legacy IAM accumulates toxic permissions. "
        "AI-ChainID executes a real-time delta synthesis: Keep (Neutral), Revoke (Grey), Grant (Indigo), and commits an <code>Ed25519-ScopeReMint</code> block, severing obsolete scopes permanently.<br/>"
        "<b>C. Automated Offboarding & Cryptographic Cutoff:</b> 4-stage guided flow displaying the complete cryptographic footprint (DID, 3 Verifiable Credentials, 3 Hardware Assets). "
        "Upon typed confirmation, an animated SVG node-graph disassembles, credentials are tombstoned, hardware is flagged <code>RETURN_PENDING</code>, and an irreversible <code>IDENTITY_CRYPTOGRAPHIC_CUTOFF</code> block is mined.",
        body_style
    ))

    story.append(PageBreak())

    # ==========================================
    # PAGE 4: HACKATHON DEFENSE & 10-STEP STORY
    # ==========================================
    story.append(Paragraph("7. Hackathon Judge Defense: Defeating Common Counter-Arguments", sec_header_style))
    
    defense_data = [
        [Paragraph("Judge Challenge / Inquiry", th_style), Paragraph("Common Misconception", th_style), Paragraph("AI-ChainID Winning Counter-Defense", th_style)],
        [
            Paragraph("<b>Why not use public Ethereum or Polygon?</b>", tb_style),
            Paragraph("<i>'Public chains offer higher decentralization.'</i>", tb_style),
            Paragraph("<b>GDPR & Enterprise Privacy:</b> Public chains leak corporate employee metadata and cannot honor GDPR 'Right to be Forgotten'. Our private consortium chain provides cryptographic immutability with microsecond finality and zero gas fees.", tb_style)
        ],
        [
            Paragraph("<b>Isn't the AI vulnerable to cold-start without training data?</b>", tb_style),
            Paragraph("<i>'ML requires months of historical security logs.'</i>", tb_style),
            Paragraph("<b>Day-0 Grounding + Unsupervised Outliers:</b> Synthetic policy grounding solves the Day-0 cold-start problem. In production, our <b>Isolation Forest</b> is completely unsupervised and continuously retrains from live telemetry buffered in the monitoring feed.", tb_style)
        ],
        [
            Paragraph("<b>Why not auto-kill high-risk tokens immediately?</b>", tb_style),
            Paragraph("<i>'High risk should always terminate access.'</i>", tb_style),
            Paragraph("<b>Operational Resilience (Anti-Self-DoS):</b> Hard auto-bans on 1-2% false positives disrupt critical hospital or banking operations. Following <b>NIST SP 800-207</b>, AI-ChainID flags elevated risk for Step-Up MFA and provides Break-Glass overrides.", tb_style)
        ]
    ]
    t_def = Table(defense_data, colWidths=[105, 130, 269])
    t_def.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_danger),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 2),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 2),
    ]))
    story.append(t_def)
    story.append(Spacer(1, 3))

    # Section 8: The 10-Step Lifecycle Story
    story.append(Paragraph("8. The 10-Step Zero-Trust Employee Journey (Day 1 to Departure)", sec_header_style))
    story.append(Paragraph(
        "<b>1. HR Profile Registration &rarr; 2. W3C DID Minted &rarr; 3. AES-256 Off-Chain PII Vault Commit &rarr; "
        "4. Verifiable Credentials Issued &rarr; 5. AI Engine 1 Least-Privilege Recommendation &rarr; "
        "6. Scoped Admin Approval (or Break-Glass Override) &rarr; 7. Blockchain Permission Mint & Hardware Asset Anchor &rarr; "
        "8. Continuous AI Security Monitoring (Telemetry Scored) &rarr; 9. Department Transfer Scope Re-Mint &rarr; "
        "10. Cryptographic Cutoff & Offboarding (DID Severed, Hardware Flagged for Return).</b>",
        body_style
    ))
    story.append(Spacer(1, 2))

    # Section 9: REST API Quick Reference Table
    story.append(Paragraph("9. Core Microservice REST API Surface", sec_header_style))
    api_data = [
        [Paragraph("Endpoint", th_style), Paragraph("Port / Service", th_style), Paragraph("Payload / Action", th_style), Paragraph("Zero-Trust Response", th_style)],
        [
            Paragraph("<code>POST /ai/recommend-permissions</code>", tb_style),
            Paragraph("FastAPI (:8000)", tb_style),
            Paragraph("Role, Dept, Credentials, Resources", tb_style),
            Paragraph("<code>ALLOW / DENY / REVIEW</code> + confidence & TTL", tb_style)
        ],
        [
            Paragraph("<code>POST /ai/risk-score</code>", tb_style),
            Paragraph("FastAPI (:8000)", tb_style),
            Paragraph("DID, Role, Resource, Hour, Device, Attempts", tb_style),
            Paragraph("Risk score (0-100), anomaly flag, risk factors", tb_style)
        ],
        [
            Paragraph("<code>POST /api/access-requests/{id}/approve</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("Request ID, Approval Note", tb_style),
            Paragraph("Status <code>APPROVED</code>, mines block, returns <code>txHash</code>", tb_style)
        ],
        [
            Paragraph("<code>POST /api/access-requests/{id}/revoke</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("Request ID, Revocation Reason", tb_style),
            Paragraph("Status <code>REVOKED</code>, appends edge kill-switch block", tb_style)
        ],
        [
            Paragraph("<code>GET /api/blockchain/verify-ledger</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("Audit Trigger", tb_style),
            Paragraph("Sequential SHA-256 Merkle chain integrity report", tb_style)
        ],
        [
            Paragraph("<code>POST /api/blockchain/verify-edge-access</code>", tb_style),
            Paragraph("Spring Boot (:8080)", tb_style),
            Paragraph("Resource ID, User ID", tb_style),
            Paragraph("Edge Gate consensus check confirming on-chain block", tb_style)
        ]
    ]
    t_api = Table(api_data, colWidths=[120, 75, 140, 169])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), c_primary),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [c_light_bg, colors.white]),
        ('GRID', (0, 0), (-1, -1), 0.5, c_border),
        ('TOPPADDING', (0, 0), (-1, -1), 1.8),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 1.8),
    ]))
    story.append(t_api)

    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"SUCCESS: Generated PDF at {target_path}")

    try:
        import shutil
        shutil.copyfile(target_path, downloads_path)
        print(f"SUCCESS: Copied PDF to {downloads_path}")
    except Exception as e:
        print(f"Notice: Could not copy to downloads ({e})")

if __name__ == "__main__":
    generate_pitch_dossier_pdf()
