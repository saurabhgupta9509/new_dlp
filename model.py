# model_api.py
from flask import Flask, request, jsonify
import torch
from transformers import (
    pipeline,
    BertTokenizerFast,
    BertForSequenceClassification,
    AutoTokenizer,
    AutoModelForSequenceClassification
)
from urllib.parse import urlparse
import json
from datetime import datetime
import traceback

app = Flask(__name__)

print("🚀 Initializing Python Model API...")

# --------------------------------------------------
# LOAD MODELS
# --------------------------------------------------
DEVICE = -1  # Start with CPU
try:
    DEVICE = 0 if torch.cuda.is_available() else -1
    print(f"📊 Device: {'GPU' if DEVICE == 0 else 'CPU'}")
except:
    print("⚠️ Could not detect GPU, using CPU")

# STAGE 1: URLBERT
URLBERT_MODEL = "CrabInHoney/urlbert-tiny-v4-malicious-url-classifier"

try:
    print("📥 Loading URLBERT model...")
    urlbert = pipeline(
        "text-classification",
        model=BertForSequenceClassification.from_pretrained(URLBERT_MODEL),
        tokenizer=BertTokenizerFast.from_pretrained(URLBERT_MODEL),
        device=DEVICE,
        top_k=None
    )
    print("✅ URLBERT loaded successfully")
except Exception as e:
    print(f"❌ Failed to load URLBERT: {e}")
    urlbert = None

URLBERT_LABELS = {
    "LABEL_0": "benign",
    "LABEL_1": "defacement",
    "LABEL_2": "malware",
    "LABEL_3": "phishing"
}

# STAGE 2: RoBERTa
try:
    print("📥 Loading RoBERTa model...")
    roberta_tokenizer = AutoTokenizer.from_pretrained("roberta-base")
    roberta_model = AutoModelForSequenceClassification.from_pretrained("roberta-base")
    roberta = pipeline(
        "text-classification",
        model=roberta_model,
        tokenizer=roberta_tokenizer,
        device=DEVICE
    )
    print("✅ RoBERTa loaded successfully")
except Exception as e:
    print(f"❌ Failed to load RoBERTa: {e}")
    roberta = None

# TRUSTED DOMAIN WHITELIST
WHITELIST = {
    "google.com",
    "www.google.com",
    "github.com",
    "openai.com",
    "microsoft.com",
    "linkedin.com",
    "facebook.com",
    "twitter.com",
    "youtube.com",
    "amazon.com",
    "stackoverflow.com",
    "wikipedia.org"
}

# --------------------------------------------------
# HELPER FUNCTIONS
# --------------------------------------------------
def get_domain(url):
    try:
        parsed = urlparse(url)
        if parsed.netloc:
            return parsed.netloc.lower()
        elif parsed.path:
            # Handle URLs like "192.168.1.1/login.php"
            return parsed.path.lower().split('/')[0]
        else:
            return url.lower()
    except:
        return ""

def is_whitelisted(url):
    domain = get_domain(url)
    for whitelisted in WHITELIST:
        if domain.endswith(whitelisted):
            return True
    return False

def urlbert_predict(url):
    if urlbert is None:
        return "benign", 0.5
    
    try:
        scores = sorted(urlbert(url)[0], key=lambda x: x["score"], reverse=True)
        label = URLBERT_LABELS[scores[0]["label"]]
        confidence = scores[0]["score"]
        return label, confidence
    except Exception as e:
        print(f"⚠️ URLBERT prediction error for {url}: {e}")
        return "benign", 0.5

def roberta_predict(url):
    if roberta is None:
        return "LABEL_0", 0.5
    
    try:
        result = roberta(url)[0]
        return result["label"], result["score"]
    except Exception as e:
        print(f"⚠️ RoBERTa prediction error for {url}: {e}")
        return "LABEL_0", 0.5

def classify_url(url):
    try:
        # Whitelist check
        if is_whitelisted(url):
            return "benign", 1.0
        
        # URLBERT scan
        t_label, t_conf = urlbert_predict(url)
        
        if t_label == "benign" and t_conf >= 0.90:
            return "benign", t_conf
        
        if t_label != "benign" and t_conf < 0.75:
            return "suspicious", t_conf
        
        # RoBERTa verification
        d_label, d_conf = roberta_predict(url)
        
        if d_conf >= 0.80:
            if d_label == "LABEL_0":
                return "benign", d_conf
            elif d_label == "LABEL_1":
                return "malicious", d_conf
            else:
                return "suspicious", d_conf
        
        return "suspicious", max(t_conf, d_conf)
        
    except Exception as e:
        print(f"❌ Error classifying URL {url}: {e}")
        return "suspicious", 0.5

# Update the analyze_urls_batch function in model.py
def analyze_urls_batch(urls):
    """
    Analyze a batch of URLs and generate security certificate with detailed URL analysis
    """
    print(f"🔍 Analyzing {len(urls)} URLs...")
    
    detailed_results = []
    stats = {
        "benign": 0,
        "malicious": 0,
        "suspicious": 0,
        "phishing": 0,
        "defacement": 0,
        "total": len(urls)
    }
    
    # Limit to 50 URLs to avoid timeout
    urls_to_analyze = urls[:50]
    
    for url in urls_to_analyze:
        try:
            # Get detailed predictions from URLBERT
            urlbert_predictions = urlbert(url)[0] if urlbert else []
            urlbert_scores = {}
            
            for pred in urlbert_predictions:
                label_name = URLBERT_LABELS[pred["label"]]
                urlbert_scores[label_name] = round(pred["score"] * 100, 2)
            
            # Get final classification
            category, confidence = classify_url(url)
            
            # Create detailed result
            result = {
                "url": url[:100],  # Truncate long URLs
                "domain": get_domain(url),
                "final_category": category,
                "final_confidence": round(confidence * 100, 2),
                "urlbert_predictions": urlbert_scores,
                "roberta_confidence": round(roberta_predict(url)[1] * 100, 2) if roberta else 0
            }
            
            detailed_results.append(result)
            stats[category] = stats.get(category, 0) + 1
            
            # Also count specific URLBERT categories
            if urlbert_scores:
                max_category = max(urlbert_scores.items(), key=lambda x: x[1])[0]
                if max_category != "benign":
                    stats[max_category] = stats.get(max_category, 0) + 1
            
        except Exception as e:
            print(f"⚠️ Error processing URL {url}: {e}")
            stats["suspicious"] = stats.get("suspicious", 0) + 1
    
    # Calculate security score
    safe_count = stats["benign"]
    risky_count = stats["malicious"] + stats["phishing"] + stats["defacement"]
    suspicious_count = stats["suspicious"]
    
    total_analyzed = safe_count + risky_count + suspicious_count
    if total_analyzed > 0:
        safety_score = (safe_count / total_analyzed) * 100
    else:
        safety_score = 100
    
    # Generate certificate data with detailed URL analysis
    security_metrics = {
        "overall_score": round(safety_score, 1),
        "grade": calculate_grade(safety_score),
        "threat_level": calculate_threat_level(safety_score),
        "safe_urls": safe_count,
        "risky_urls": risky_count,
        "suspicious_urls": suspicious_count,
        "total_urls_analyzed": total_analyzed,
        "analysis_date": datetime.now().isoformat(),
        "model_confidence": "high" if total_analyzed > 10 else "medium",
        "checks_passed": safe_count,
        "checks_total": total_analyzed
    }
    
    detailed_analysis = {
        "analysis_method": "dual_model_ai",
        "models_used": ["URLBERT", "RoBERTa"],
        "detailed_url_results": detailed_results,  # Store all detailed results
        "sample_results": detailed_results[:10],  # Top 10 for quick view
        "statistics": stats,
        "timestamp": datetime.now().isoformat()
    }
    
    recommendations = generate_recommendations(stats, safety_score)
    
    return {
        "securityMetrics": security_metrics,
        "detailedAnalysis": detailed_analysis,
        "recommendations": recommendations
    }
def calculate_grade(score):
    if score >= 90: return "A+"
    if score >= 80: return "A"
    if score >= 70: return "B"
    if score >= 60: return "C"
    if score >= 50: return "D"
    return "F"

def calculate_threat_level(score):
    if score >= 80: return "low"
    if score >= 60: return "medium"
    return "high"

def generate_recommendations(stats, safety_score):
    recommendations = []
    
    if safety_score < 70:
        recommendations.append({
            "priority": "high",
            "action": "Review browsing habits",
            "description": f"High risk activity detected ({stats.get('risky_urls', 0)} risky URLs)"
        })
    
    if stats.get("suspicious", 0) > 5:
        recommendations.append({
            "priority": "medium",
            "action": "Enable enhanced security monitoring",
            "description": "Multiple suspicious URLs detected"
        })
    
    if stats.get("total", 0) == 0:
        recommendations.append({
            "priority": "low",
            "action": "Increase monitoring",
            "description": "No recent URL activity detected"
        })
    
    # Always include basic recommendation
    recommendations.append({
        "priority": "low",
        "action": "Keep software updated",
        "description": "Ensure all security software is up to date"
    })
    
    return recommendations

# --------------------------------------------------
# FLASK API ENDPOINTS
# --------------------------------------------------
@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                "success": False,
                "message": "No JSON data provided",
                "data": None
            }), 400
        
        urls = data.get('urls', [])
        
        if not urls:
            return jsonify({
                "success": False,
                "message": "No URLs provided",
                "data": None
            }), 400
        
        print(f"📨 Received request with {len(urls)} URLs")
        
        # Analyze URLs
        certificate_data = analyze_urls_batch(urls)
        
        print(f"✅ Analysis complete. Score: {certificate_data['securityMetrics']['overall_score']}")
        
        return jsonify({
            "success": True,
            "message": f"Analyzed {len(urls)} URLs",
            "data": certificate_data
        })
        
    except Exception as e:
        print(f"❌ Error in prediction: {e}")
        traceback.print_exc()
        return jsonify({
            "success": False,
            "message": str(e),
            "data": None
        }), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "healthy",
        "model": "URL Security Classifier",
        "device": "GPU" if DEVICE == 0 else "CPU",
        "timestamp": datetime.now().isoformat(),
        "models_loaded": urlbert is not None and roberta is not None
    })

@app.route('/test', methods=['GET'])
def test():
    """Test endpoint with sample URLs"""
    test_urls = [
        "https://www.google.com",
        "https://github.com",
        "http://malicious-site.com/phishing",
        "http://192.168.1.1/login.php"
    ]
    
    try:
        certificate_data = analyze_urls_batch(test_urls)
        return jsonify({
            "success": True,
            "message": "Test successful",
            "data": certificate_data
        })
    except Exception as e:
        return jsonify({
            "success": False,
            "message": f"Test failed: {e}",
            "data": None
        }), 500

if __name__ == '__main__':
    print("🌐 Starting Flask API on http://0.0.0.0:5000")
    app.run(host='0.0.0.0', port=5000, debug=True, threaded=True)