package com.example.data.sample

import com.example.model.*

object IksSampleData {

    val domains = listOf(
        KnowledgeDomain(
            id = "math",
            title = "Indian Mathematics",
            titleHi = "भारतीय गणित",
            titleMr = "भारतीय गणित",
            description = "From decimal place-value, zero, and Sulba geometry to Madhava's infinite series calculus.",
            descriptionHi = "दशमलव स्थान-मान, शून्य, शुल्ब ज्यामिति से लेकर माधव की अनन्त श्रेणी कलन तक।",
            descriptionMr = "दशांश स्थान-मूल्य, शून्य, शुल्ब भूमिती ते माधव यांच्या अनंत श्रेणी कलनापर्यंत.",
            iconName = "Calculate",
            lessonCount = 8,
            courseCount = 2,
            difficulty = "Beginner to Advanced",
            categoryGroup = "Exact Sciences"
        ),
        KnowledgeDomain(
            id = "astronomy",
            title = "Indian Astronomy",
            titleHi = "भारतीय खगोलशास्त्र",
            titleMr = "भारतीय खगोलशास्त्र",
            description = "Observational astronomy, Nakshatras, planetary models, Aryabhata's axial rotation, and Jantar Mantar.",
            descriptionHi = "वेधशालाएं, नक्षत्र, ग्रहीय गति, आर्यभट्ट का पृथ्वी घूर्णन सिद्धांत एवं जन्तर-मन्तर।",
            descriptionMr = "नक्षत्र, ग्रहगती मॉडेल्स, आर्यभट यांचे पृथ्वीचे परिवलन आणि जंतर मंतर वेधशाळा.",
            iconName = "AutoAwesome",
            lessonCount = 6,
            courseCount = 1,
            difficulty = "Intermediate",
            categoryGroup = "Exact Sciences"
        ),
        KnowledgeDomain(
            id = "ayurveda",
            title = "Ayurveda & Health",
            titleHi = "आयुर्वेद एवं स्वास्थ्य",
            titleMr = "आयुर्वेद आणि आरोग्य",
            description = "Holistic health, Tridosha physiology, preventive Dinacharya, surgical techniques of Sushruta.",
            descriptionHi = "समग्र स्वास्थ्य, त्रिदोष शरीरक्रिया, दिनचर्या-ऋतुचर्या एवं सुश्रुत की शल्यक्रिया।",
            descriptionMr = "त्रिदोष सिद्धांत, दिनचर्या, ऋतूचर्या आणि सुश्रुत यांची प्राचीन शस्त्रक्रिया.",
            iconName = "Healing",
            lessonCount = 7,
            courseCount = 1,
            difficulty = "Beginner",
            categoryGroup = "Life Sciences"
        ),
        KnowledgeDomain(
            id = "yoga",
            title = "Yoga & Consciousness",
            titleHi = "योग एवं चेतना",
            titleMr = "योग आणि जाणीव",
            description = "Patanjali's Ashtanga Yoga, psychology of mind (Chitta Vritti), and meditative contemplation.",
            descriptionHi = "पतंजलि का अष्टांग योग, चित्त-वृत्ति निरोध एवं ध्यानात्मक चेतना विज्ञान।",
            descriptionMr = "पतंजली यांचे अष्टांग योग, मनाचे मानसशास्त्र आणि ध्यानधारणा.",
            iconName = "SelfImprovement",
            lessonCount = 5,
            courseCount = 1,
            difficulty = "Beginner to Intermediate",
            categoryGroup = "Mind & Spirit"
        ),
        KnowledgeDomain(
            id = "philosophy",
            title = "Indian Philosophy",
            titleHi = "भारतीय दर्शन",
            titleMr = "भारतीय तत्त्वज्ञान",
            description = "The six orthodox Darshanas (Nyaya, Vaisheshika, Samkhya, Yoga, Mimamsa, Vedanta) & logic.",
            descriptionHi = "षड्दर्शन (न्याय, वैशेषिक, सांख्य, योग, मीमांसा, वेदान्त) एवं ज्ञानमीमांसा।",
            descriptionMr = "षड्दर्शने, भारतीय तर्कशास्त्र आणि प्रमाण सिद्धांत.",
            iconName = "Psychology",
            lessonCount = 6,
            courseCount = 1,
            difficulty = "Intermediate to Advanced",
            categoryGroup = "Philosophy & Logic"
        ),
        KnowledgeDomain(
            id = "linguistics",
            title = "Sanskrit & Linguistics",
            titleHi = "संस्कृत एवं भाषाविज्ञान",
            titleMr = "संस्कृत आणि भाषाविज्ञान",
            description = "Panini's formal generative grammar (Ashtadhyayi), phonetics (Shiksha), semantic theories.",
            descriptionHi = "पाणिनि की अष्टाध्यायी (औपचारिक व्याकरण), शिक्षा (ध्वनिविज्ञान) एवं अर्थविचार।",
            descriptionMr = "पाणिनी यांचे अष्टाध्यायी व्याकरण, ध्वनिशास्त्र आणि आधुनिक भाषाविज्ञान.",
            iconName = "Translate",
            lessonCount = 5,
            courseCount = 1,
            difficulty = "Intermediate",
            categoryGroup = "Language & Semiotics"
        ),
        KnowledgeDomain(
            id = "architecture",
            title = "Vastu & Architecture",
            titleHi = "वास्तु एवं स्थापत्य",
            titleMr = "वास्तू आणि स्थापत्यकला",
            description = "Nagara, Dravida, and Vesara temple architecture, rock-cut monoliths (Ellora), stepwell acoustics.",
            descriptionHi = "नागर, द्रविड़ एवं वेसर मंदिर स्थापत्य, एलोरा शैलकृत स्मारक, बावड़ियाँ एवं ध्वनिकी।",
            descriptionMr = "नागर, द्रविड व वेसर स्थापत्य, कैलास मंदिर वेरुळ आणि जलस्थापत्य.",
            iconName = "Apartment",
            lessonCount = 5,
            courseCount = 1,
            difficulty = "Intermediate",
            categoryGroup = "Art & Engineering"
        ),
        KnowledgeDomain(
            id = "metallurgy",
            title = "Metallurgy & Materials",
            titleHi = "धातुकर्म एवं पदार्थ विज्ञान",
            titleMr = "धातुकर्म आणि पदार्थ विज्ञान",
            description = "Wootz crucible steel, rust-free Delhi Iron Pillar, ancient distillation of zinc at Zawar.",
            descriptionHi = "वूट्ज इस्पात, दिल्ली का जंग-प्रतिरोधी लौह स्तंभ, जावर में जस्ते का प्राचीन आसवन।",
            descriptionMr = "वूट्झ स्टील, दिल्लीचा लोहस्तंभ आणि जस्त निष्कर्षण तंत्रज्ञान.",
            iconName = "Build",
            lessonCount = 4,
            courseCount = 1,
            difficulty = "Intermediate",
            categoryGroup = "Applied Engineering"
        ),
        KnowledgeDomain(
            id = "water",
            title = "Water Management & Ecology",
            titleHi = "जल प्रबंधन एवं पर्यावरण",
            titleMr = "जलव्यवस्थापन आणि पर्यावरण",
            description = "Harappan drainage, stepwells (Baolis), Chola lake networks (Grand Anicut), community reservoirs.",
            descriptionHi = "हड़प्पाकालीन जल निकास, सीढ़ीदार कुएं, चोल कालीन भव्य जलाशय एवं कल्लाणई बांध।",
            descriptionMr = "हडप्पा जलसंस्कृती, बारवांचे तंत्रज्ञान आणि ग्रँड अनिकट बंधारा.",
            iconName = "WaterDrop",
            lessonCount = 4,
            courseCount = 1,
            difficulty = "Beginner",
            categoryGroup = "Applied Engineering"
        ),
        KnowledgeDomain(
            id = "agriculture",
            title = "Traditional Agriculture",
            titleHi = "पारंपरिक कृषि एवं वृक्षाdefault",
            titleMr = "पारंपरिक शेती व वृक्षायुर्वेद",
            description = "Vrikshayurveda, seasonal crop rotation (Krishi-Parashara), organic soil health, seed conservation.",
            descriptionHi = "वृक्षायुर्वेद, कृषि-पाराशर अनुसार ऋतुचक्र, बीज संवर्धन एवं मृदा पोषण।",
            descriptionMr = "कृषी-पराशर, वृक्षायुर्वेद, देशी बियाणे संवर्धन आणि सेंद्रिय शेती.",
            iconName = "Spa",
            lessonCount = 4,
            courseCount = 1,
            difficulty = "Beginner",
            categoryGroup = "Life Sciences"
        ),
        KnowledgeDomain(
            id = "arts",
            title = "Performing Arts & Natya",
            titleHi = "नाट्य एवं संगीत कला",
            titleMr = "नाट्य आणि संगीत",
            description = "Bharata Muni's Natyashastra, Navarasa emotion theory, melodic frameworks of Ragas.",
            descriptionHi = "भरतमुनि का नाट्यशास्त्र, नवरस सिद्धांत, शास्त्रीय संगीत एवं ताल संरचना।",
            descriptionMr = "नाट्यशास्त्र, नवरस संकल्पना आणि राग-ताल व्यवस्था.",
            iconName = "MusicNote",
            lessonCount = 4,
            courseCount = 1,
            difficulty = "Beginner",
            categoryGroup = "Culture & Arts"
        ),
        KnowledgeDomain(
            id = "governance",
            title = "Polity & Governance",
            titleHi = "शासन एवं राजनीति शास्त्र",
            titleMr = "राज्यशास्त्र व प्रशासन",
            description = "Kautilya's Arthashastra, Saptanga theory of state, public administration, ethical welfare.",
            descriptionHi = "कौटिल्य का अर्थशास्त्र, सप्तांग राज्य सिद्धांत, लोक-प्रशासन एवं कर-व्यवस्था।",
            descriptionMr = "कौटिल्य यांचे अर्थशास्त्र, सप्तांग सिद्धांत आणि लोककल्याणकारी प्रशासन.",
            iconName = "AccountBalance",
            lessonCount = 4,
            courseCount = 1,
            difficulty = "Intermediate",
            categoryGroup = "Social Sciences"
        )
    )

    val courses = listOf(
        Course(
            id = "course_math_1",
            domainId = "math",
            title = "Indian Mathematics: Sulba to Calculus",
            titleHi = "भारतीय गणित: शुल्ब से कलन तक",
            titleMr = "भारतीय गणित: शुल्ब ते कलन",
            description = "Trace 3,000 years of mathematical genius: Sulba geometry, zero and decimal place-value, Kuttaka algebra, and Madhava's infinite series.",
            difficulty = "Intermediate",
            durationMinutes = 90,
            lessonCount = 5,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Understand the Vedic Sulba Sutras and Baudhayana theorem",
                "Learn the decimal place-value and zero's mathematical definitions by Brahmagupta",
                "Explore Aryabhata's calculation of Pi and Kuttaka method",
                "Discover Kerala School of Mathematics calculus series centuries before Newton & Leibniz"
            ),
            prerequisites = "Basic secondary school arithmetic and geometry",
            sourceTitle = "Indian Mathematics and Astronomy: Some Landmarks",
            sourceInstitution = "Indian National Science Academy (INSA) & IIT Gandhinagar",
            progressPercent = 40
        ),
        Course(
            id = "course_astro_1",
            domainId = "astronomy",
            title = "Indian Astronomy: Observing the Cosmos",
            titleHi = "भारतीय खगोलशास्त्र: आकाशीय वेध",
            titleMr = "भारतीय खगोलशास्त्र: विश्वाचे निरीक्षण",
            description = "Explore stellar tracking, lunar mansions (Nakshatras), Aryabhata's axial rotation of Earth, and the precision instruments of Jantar Mantar.",
            difficulty = "Intermediate",
            durationMinutes = 75,
            lessonCount = 4,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Master the 27 Nakshatras and Vedic calendrical cycles",
                "Study Aryabhata's heliocentric intuitions and planetary diameter calculations",
                "Understand Siddhantic astronomical treatises (Surya Siddhanta)",
                "Examine Maharaja Jai Singh's monumental stone sundials and yantras"
            ),
            prerequisites = "Basic interest in astronomy and geography",
            sourceTitle = "A History of Indian Astronomy: The Sūtras to the Modern Era",
            sourceInstitution = "National Institute of Advanced Studies (NIAS)",
            progressPercent = 25
        ),
        Course(
            id = "course_ayur_1",
            domainId = "ayurveda",
            title = "Ayurveda: The Classical Healing Tradition",
            titleHi = "आयुर्वेद: शास्त्रीय चिकित्सा परंपरा",
            titleMr = "आयुर्वेद: अभिजात उपचार परंपरा",
            description = "A scientific study of Tridosha physiological balance, Charaka's internal medicine, Sushruta's surgical instruments, and preventive lifestyle.",
            difficulty = "Beginner",
            durationMinutes = 80,
            lessonCount = 4,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Grasp the Tridosha (Vata, Pitta, Kapha) homeostasis model",
                "Analyze Charaka Samhita's etiology and clinical methodology",
                "Examine Sushruta's surgical pioneering, including rhinoplasty and cataract surgery",
                "Learn practical Dinacharya (circadian routines) for contemporary well-being"
            ),
            prerequisites = "No prior prerequisites needed",
            sourceTitle = "Foundations of Ayurveda & Classical Medical Texts",
            sourceInstitution = "Ministry of AYUSH & All India Institute of Ayurveda",
            progressPercent = 75
        ),
        Course(
            id = "course_phil_1",
            domainId = "philosophy",
            title = "The Six Classical Systems of Indian Philosophy",
            titleHi = "भारतीय दर्शन के षड्दर्शन",
            titleMr = "भारतीय तत्त्वज्ञानाची षड्दर्शने",
            description = "A systematic exploration of Nyaya logic, Vaisheshika atomic pluralism, Samkhya evolutionary cosmology, Yoga, Mimamsa, and Vedanta metaphysics.",
            difficulty = "Intermediate",
            durationMinutes = 85,
            lessonCount = 4,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Distinguish Astika (orthodox) and Nastika (heterodox) philosophical systems",
                "Understand Nyaya's 4 Pramanas (epistemological tools of valid truth)",
                "Examine Kanada's Vaisheshika atomic theory (Paramanu)",
                "Study Shankara and Ramanuja's Vedanta non-dualism"
            ),
            prerequisites = "Curiosity about epistemology and metaphysics",
            sourceTitle = "Outlines of Indian Philosophy",
            sourceInstitution = "Prof. M. Hiriyanna / Oxford University Press & ICPR",
            progressPercent = 10
        ),
        Course(
            id = "course_ling_1",
            domainId = "linguistics",
            title = "Paninian Linguistics & Computer Science",
            titleHi = "पाणिनीय भाषाविज्ञान एवं संगणक शास्त्र",
            titleMr = "पाणिनीय व्याकरण व संगणक विज्ञान",
            description = "Discover the world's first formal generative rule system: Panini's Ashtadhyayi, context-free grammars, and phonetic classification.",
            difficulty = "Advanced",
            durationMinutes = 70,
            lessonCount = 3,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Understand the 4,000 algorithmic sutras of Ashtadhyayi",
                "Analyze the Shiva Sutras as phonetic classification arrays",
                "Discover the direct relationship between Panini's rules and Backus-Naur Form (BNF)"
            ),
            prerequisites = "Introductory logic or linguistics",
            sourceTitle = "Sanskrit Computational Linguistics",
            sourceInstitution = "Springer & Rashtriya Sanskrit Sansthan",
            progressPercent = 0
        ),
        Course(
            id = "course_arch_1",
            domainId = "architecture",
            title = "Temple Architecture & Sacred Geometry",
            titleHi = "मंदिर स्थापत्य एवं ज्यामितीय विन्यास",
            titleMr = "मंदिर स्थापत्य आणि वास्तुशास्त्र",
            description = "Explore Vastu Purusha Mandala layout geometry, Nagara and Dravida temple forms, and the monolithic rock-cut wonder of Kailasa at Ellora.",
            difficulty = "Beginner",
            durationMinutes = 60,
            lessonCount = 3,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Deconstruct the grid mathematics of the Vastu Purusha Mandala",
                "Compare Northern Nagara (Curvilinear Shikhara) and Southern Dravida (Vimana) styles",
                "Understand the top-down rock excavation engineering of Kailasa Temple (Cave 16)"
            ),
            prerequisites = "None",
            sourceTitle = "The Hindu Temple (Vols. I & II)",
            sourceInstitution = "Stella Kramrisch / Archaeological Survey of India (ASI)",
            progressPercent = 0
        ),
        Course(
            id = "course_metal_1",
            domainId = "metallurgy",
            title = "Ancient Indian Metallurgy: Wootz & Zinc",
            titleHi = "प्राचीन भारतीय धातुकर्म: वूट्ज एवं जस्ता",
            titleMr = "प्राचीन भारतीय धातूशास्त्र",
            description = "Uncover how ancient Indians produced high-carbon crucible steel (Wootz), rust-resistant wrought iron, and industrialized zinc distillation.",
            difficulty = "Intermediate",
            durationMinutes = 65,
            lessonCount = 3,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Analyze the micro-structure and high phosphorus content of the Delhi Iron Pillar",
                "Study the crucible melting process of South Indian Wootz steel",
                "Examine the retort distillation furnaces at Zawar (Rajasthan) from the 12th century"
            ),
            prerequisites = "Basic high-school chemistry",
            sourceTitle = "India's Legendary Wootz Steel & Ancient Metallurgy",
            sourceInstitution = "Prof. Sharada Srinivasan / National Institute of Advanced Studies",
            progressPercent = 0
        ),
        Course(
            id = "course_water_1",
            domainId = "water",
            title = "Traditional Water Harvesting & Hydraulics",
            titleHi = "पारंपरिक जल संरक्षण एवं जल स्थापत्य",
            titleMr = "पारंपरिक जलसंधारण आणि विहिरींचे तंत्रज्ञान",
            description = "From Harappan reservoir grids to ornate subterranean stepwells (Vavs) and the 2nd-century Grand Anicut (Kallanai) dam on the Kaveri.",
            difficulty = "Beginner",
            durationMinutes = 55,
            lessonCount = 3,
            coverImageRes = "iks_hero_banner",
            objectives = listOf(
                "Inspect Dholavira's rock-cut storm-water harvesting networks",
                "Understand the thermal cooling and communal social function of stepwells",
                "Study the stone gravity dam engineering of Kallanai built under King Karikala Chola"
            ),
            prerequisites = "None",
            sourceTitle = "Dying Wisdom: Rise, Fall and Potential of Traditional Water Systems",
            sourceInstitution = "Centre for Science and Environment (CSE)",
            progressPercent = 0
        )
    )

    val lessons = listOf(
        Lesson(
            id = "lesson_math_101",
            courseId = "course_math_1",
            title = "The Sulba Sutras: Ancient Vedic Geometry",
            titleHi = "शुल्ब सूत्र: प्राचीन वैदिक ज्यामिति",
            titleMr = "शुल्ब सूत्रे: प्राचीन भूमिती",
            readingTimeMinutes = 8,
            order = 1,
            overview = "The Sulba Sutras (authored by Baudhayana, Apastamba, Manava, and Katyayana) contain the earliest known geometric statements of the Pythagorean theorem and precise calculations for irrational square roots.",
            keyConcepts = listOf(
                "Baudhayana Theorem (Diagonal theorem)",
                "Approximation of √2 to 5 decimal places",
                "Squaring the circle and circling the square",
                "Sacred geometry of Agnicayana fire altars"
            ),
            contentMarkdown = """
# The Sulba Sutras: Ancient Vedic Geometry

The term **Śulba** derives from the Sanskrit root *śulb*, meaning to measure or rope. These texts are appendices to the *Śrauta Sūtras* and served as practical manuals for constructing ritual fire altars (*vedis*) with exact geometric proportions.

---

## 1. The Baudhayana Theorem
Decades to centuries before Pythagoras of Samos (c. 570–495 BCE), **Baudhayana** (c. 800–600 BCE) articulated the geometric relationship of right-angled triangles in *Baudhayana Śulba Sūtra (1.48)*:

> **"दीर्घचतुरश्रस्याक्ष्णया रज्जुः पार्श्वमानी तिर्यङ्मानी च यत्पृथग्भूते कुरुतस्तदुभयं करोति॥"**
> 
> *"The diagonal cord of an oblong (rectangle) produces both areas which its horizontal and vertical sides make separately."*

In modern algebraic notation, if a rectangle has sides of lengths a and b with diagonal d:
d² = a² + b²

Baudhayana explicitly documented integer Pythagorean triples, including:
* (3, 4, 5)
* (5, 12, 13)
* (7, 24, 25)
* (8, 15, 17)
* (12, 35, 37)

---

## 2. Rational Approximation of the Square Root of 2
To construct a square with double the area of a given square, Baudhayana and Apastamba provided an extraordinary rational fraction:

> **"प्रमाणं तृतीयेन वर्धयेत्तच्च चतुर्थेनात्मचतुस्त्रिंशोनेन सविशेषः॥"**
> 
> *"Increase the measure by its third, and this third by its own fourth less its thirty-fourth part; that is its diagonal with an excess."*

In fractional formulation:
√2 ≈ 1 + 1/3 + 1/(3 × 4) - 1/(3 × 4 × 34)

√2 ≈ 1 + 1/3 + 1/12 - 1/408 = 577/408 ≈ 1.414215686...

Compared to the actual value √2 ≈ 1.414213562..., this approximation has an error of less than 0.00015%, accurate to five decimal places without decimal notation!

---

## 3. Geometric Transformations
The Sulba geometers developed rigorous ruler-and-cord algorithms for:
1. Converting a rectangle into a square of identical area.
2. Converting a square into a circle (*Circling the Square*).
3. Doubling or tripling the area of an altar while retaining its shape (*Falcon-shaped altar: Śyenaciti*).
            """.trimIndent(),
            contentHindi = "शुल्ब सूत्र वैदिक काल के ज्यामितीय ग्रंथ हैं। बौधायन शुल्ब सूत्र में समकोण त्रिभुज के कर्ण का नियम दिया गया है...",
            contentMarathi = "शुल्ब सूत्रे ही प्राचीन वैदिक भूमितीवरील मार्गदर्शक पुस्तके आहेत. यामध्ये काटकोन त्रिकोणाचा कर्ण सिद्धांत दिला आहे...",
            historicalContext = "Composed between c. 800 and 500 BCE during the late Vedic period across Northern India. Altars were required to adhere to exact spatial dimensions to preserve acoustic resonance and ceremonial symmetry.",
            keyFigures = listOf("Baudhayana (c. 800 BCE)", "Apastamba (c. 600 BCE)", "Katyayana (c. 500 BCE)"),
            keyTexts = listOf("Baudhayana Sulba Sutra", "Apastamba Sulba Sutra", "Manava Sulba Sutra"),
            sources = listOf(
                SourceCitation(
                    title = "The Crest of the Peacock: Non-European Roots of Mathematics",
                    authorOrEditor = "George Gheverghese Joseph",
                    institutionOrPublisher = "Princeton University Press",
                    publicationYear = "2011",
                    referenceText = "Chapters 8 & 9 on Vedic Geometry and the Sulba Sutras."
                ),
                SourceCitation(
                    title = "Geometry in Ancient and Medieval India",
                    authorOrEditor = "T.A. Sarasvati Amma",
                    institutionOrPublisher = "Motilal Banarsidass",
                    publicationYear = "1979",
                    referenceText = "Detailed mathematical proofs of Baudhayana and Apastamba algorithms."
                )
            ),
            relatedTopicIds = listOf("lesson_math_102", "lesson_math_103")
        ),
        Lesson(
            id = "lesson_math_102",
            courseId = "course_math_1",
            title = "Zero (Shunya) & The Decimal Place-Value System",
            titleHi = "शून्य एवं दशमलव स्थान-मान पद्धति",
            titleMr = "शून्य आणि दशांश स्थान-मूल्य पद्धती",
            readingTimeMinutes = 7,
            order = 2,
            overview = "How Indian mathematicians revolutionized global civilization by transforming zero from a mere punctuation marker into a fully operational number with arithmetic rules.",
            keyConcepts = listOf(
                "Shunya as both void and operator",
                "Decimal place-value notation (Sthanat sthanam dashagunam)",
                "Brahmagupta's rules of arithmetic involving zero",
                "Transmission via Al-Khwarizmi to Europe"
            ),
            contentMarkdown = """
# Zero (Śūnya) & The Decimal Place-Value Revolution

The concept of **zero** and the **decimal place-value system** represents one of India's greatest intellectual gifts to humanity. Renowned French mathematician Pierre-Simon Laplace observed:

> *"It is India that gave us the ingenious method of expressing all numbers by means of ten symbols, each symbol receiving a value of position as well as an absolute value... a profound and important idea."*

---

## 1. Zero in Indian Philosophy and Mathematics
The Sanskrit word **Śūnya** (शून्य) translates to "empty" or "void", rooted in Buddhist and Upanishadic philosophical inquiries into the nature of emptiness. In Pingala's *Chandahsutra* (c. 3rd–2nd century BCE), zero is used in prosodic combinatorics as a marker.

In the **Bakhshali Manuscript** (radiocarbon-dated from the 3rd to 8th century CE), a solid dot (•) represents zero as a place-holder in complex calculations.

---

## 2. Brahmagupta: Mathematical Formalization (628 CE)
In his masterwork *Brāhmasphuṭasiddhānta* (Chapter 18), **Brahmagupta** became the first mathematician in world history to establish formal arithmetic rules for operations with zero and negative numbers (*Kshaya/Rina*):

1. **Addition:**
   * a + 0 = a
   * 0 + 0 = 0
2. **Subtraction:**
   * a - 0 = a
   * 0 - a = -a
3. **Multiplication:**
   * a × 0 = 0
   * 0 × 0 = 0
4. **Division:**
   * 0 / a = 0
   * Brahmagupta defined a / 0 as *Tacchhiddam* (having zero as divisor). While he noted 0 / 0 = 0 (later corrected by modern calculus as indeterminate), his insight laid the groundwork for algebraic analysis.

---

## 3. Global Transmission
Indian numerals traveled to Baghdad under Caliph Al-Mansur (8th century CE), where Persian scholar **Muhammad ibn Musa al-Khwarizmi** authored *Kitab al-Jam' wa'l-tafriq bi-hisab al-Hind* (Book of Addition and Subtraction According to the Hindu Calculation). Through Fibonacci's *Liber Abaci* (1202 CE), the system displaced Roman numerals across Europe.
            """.trimIndent(),
            contentHindi = "शून्य और दशमलव स्थान-मान पद्धति का विकास भारत में हुआ। ब्रह्मगुप्त ने ६२८ ईस्वी में शून्य के गणितीय नियमों को प्रतिपादित किया...",
            contentMarathi = "शून्य ही संकल्पना केवळ रिकामी जागा नसून एक स्वतंत्र संख्या आहे हे ब्रह्मगुप्तांनी इ.स. ६२८ मध्ये सिद्ध केले...",
            historicalContext = "Formalized during the classical Gupta and Post-Gupta eras in Ujjain and Kusumapura. Preserved physically in the birch-bark Bakhshali Manuscript discovered in 1881 in modern Peshawar.",
            keyFigures = listOf("Pingala (c. 200 BCE)", "Brahmagupta (598–668 CE)", "Aryabhata I (476–550 CE)"),
            keyTexts = listOf("Brahmasphutasiddhanta", "Bakhshali Manuscript", "Aryabhatiya"),
            sources = listOf(
                SourceCitation(
                    title = "Brahmasphutasiddhanta of Brahmagupta (Vol. IV)",
                    authorOrEditor = "Ram Swarup Sharma",
                    institutionOrPublisher = "Indian Institute of Astronomical and Sanskrit Research",
                    publicationYear = "1966",
                    referenceText = "Chapter 18: Kuttakadhyaya (Rules for zero and negative quantities)."
                ),
                SourceCitation(
                    title = "The Nothing that Is: A Natural History of Zero",
                    authorOrEditor = "Robert Kaplan",
                    institutionOrPublisher = "Oxford University Press",
                    publicationYear = "2000",
                    referenceText = "Chapters 3–5 documenting Indian transmission to the Islamic world."
                )
            ),
            relatedTopicIds = listOf("lesson_math_101", "lesson_math_103")
        ),
        Lesson(
            id = "lesson_math_103",
            courseId = "course_math_1",
            title = "Aryabhata's Mathematics: Pi and The Kuttaka Algorithm",
            titleHi = "आर्यभट्ट: पाई का मान एवं कुट्टक विधि",
            titleMr = "आर्यभट: पायचे मूल्य आणि कुट्टक पद्धती",
            readingTimeMinutes = 8,
            order = 3,
            overview = "Aryabhata's calculation of Pi accurate to 4 decimal places, trigonometry table of sines (Jya), and the Kuttaka pulverized algorithm for linear Diophantine equations.",
            keyConcepts = listOf(
                "Approximation of Pi as 3.1416 (asanna)",
                "Jya and Kojya: Foundations of trigonometry",
                "Kuttaka (The Pulverizer) for ax - by = c",
                "Alphabetical numerical notation (Aryabhata code)"
            ),
            contentMarkdown = """
# Aryabhata: Pi, Trigonometry, and the Pulverizer (499 CE)

At age 23, in 499 CE, **Aryabhata I** composed the compact 121-verse masterpiece *Āryabhaṭīya* in Kusumapura (modern Patna).

---

## 1. Approximation of Pi (π)
In *Ganitapada*, verse 10, Aryabhata states:

> **"चतुरधिकं शतमष्टगुणं द्वाषष्टिस्तथा सहस्राणाम्।**
> **अयुतद्वयविष्कम्भस्यासन्नो वृत्तपरिणाहः॥"**
> 
> *"Add 4 to 100, multiply by 8, and add 62,000. By this rule the circumference of a circle of diameter 20,000 is approximately known."*

Calculation:
Circumference = (100 + 4) × 8 + 62000 = 832 + 62000 = 62832
Diameter = 20000

π ≈ 62832 / 20000 = 3.1416

Crucially, Aryabhata used the word **āsanna** (approximative), recognizing that the ratio of circumference to diameter cannot be expressed as an exact rational fraction—an intuitive anticipation of irrational numbers!

---

## 2. Indian Trigonometry: Jya and Kotijya
Aryabhata defined trigonometric functions based on chord lengths for a circle of radius R = 3438 minutes of arc (1 radian ≈ 3438'):
* **Jya** corresponds to modern R * sin(θ).
* **Kotijya** corresponds to R * cos(θ).
* **Utkrama-jya** corresponds to versine R * (1 - cos(θ)).

Through Arab translations, *Jya* became *Jiba*, which was mistakenly read as *Jaib* (pocket/bay) and subsequently translated into Latin as **Sinus**, the origin of the word **Sine**!

---

## 3. The Kuttaka Algorithm
Aryabhata invented the **Kuṭṭaka** (literally "pulverizer") method to solve indeterminate linear equations of the form:
ax - by = c
where a, b, c are integers. This is equivalent to finding integer solutions to astronomical periodicity cycles. The algorithm anticipated the modern Extended Euclidean Algorithm by over a thousand years.
            """.trimIndent(),
            contentHindi = "आर्यभट ने ४९९ ईस्वी में आर्यभटीय ग्रंथ रचा। इसमें पाई का मान ३.१४१६ बताया और ज्या (साइन) सारणी तैयार की...",
            contentMarathi = "आर्यभटांनी वयाच्या अवघ्या २३ व्या वर्षी आर्यभटीय हा ग्रंथ लिहिला. यात पायचे मूल्य ३.१४१६ दिले...",
            historicalContext = "Composed during the zenith of the Gupta Golden Age in Pataliputra / Kusumapura. Aryabhata headed the astronomical research centre at Nalanda University according to later commentaries.",
            keyFigures = listOf("Aryabhata I (476–550 CE)", "Bhaskara I (600–680 CE)"),
            keyTexts = listOf("Aryabhatiya", "Maha-Bhaskariya"),
            sources = listOf(
                SourceCitation(
                    title = "The Aryabhatiya of Aryabhata: An Ancient Indian Work on Mathematics and Astronomy",
                    authorOrEditor = "Walter Eugene Clark",
                    institutionOrPublisher = "University of Chicago Press",
                    publicationYear = "1930",
                    referenceText = "Translation and commentary of Ganitapada verses 10–33."
                )
            ),
            relatedTopicIds = listOf("lesson_math_101", "lesson_math_104")
        ),
        Lesson(
            id = "lesson_math_104",
            courseId = "course_math_1",
            title = "Madhava of Sangamagrama & The Kerala School of Calculus",
            titleHi = "संगमग्राम के माधव एवं केरल गणित परंपरा",
            titleMr = "संगमग्रामचे माधव आणि केरळ गणित परंपरा",
            readingTimeMinutes = 8,
            order = 4,
            overview = "Centuries before Newton, Leibniz, and Gregory, Madhava of Sangamagrama (c. 1340–1425 CE) founded mathematical analysis and formulated infinite series for sine, cosine, and arctangent.",
            keyConcepts = listOf(
                "Madhava-Leibniz infinite series for Pi",
                "Madhava-Newton series for Sine and Cosine",
                "Jyeshthadeva's Yuktibhasha: World's first calculus textbook",
                "Summation of infinitesimals (Samkalita)"
            ),
            contentMarkdown = """
# Madhava of Sangamagrama & The Kerala School of Calculus

In southwest India, between the 14th and 16th centuries, **Madhava of Sangamagrama** (c. 1340–1425 CE) and his lineage (Parameshvara, Nilakantha Somayaji, Jyeshthadeva) accomplished one of the greatest leaps in intellectual history: the transition from finite mathematics to the **calculus of the infinite**.

---

## 1. The Madhava-Gregory-Leibniz Series for π
Madhava derived the infinite series for the arctangent function:
arctan(x) = x - x^3/3 + x^5/5 - x^7/7 + ...

For x = 1, where arctan(1) = π/4:
π/4 = 1 - 1/3 + 1/5 - 1/7 + 1/9 - ...

In Europe, James Gregory rediscovered this series in 1671, and Gottfried Wilhelm Leibniz in 1673. Madhava had codified this formula **three hundred years earlier**.

---

## 2. Madhava's Sine and Cosine Power Series
Madhava formulated the exact polynomial expansions for sin(x) and cos(x):
sin(x) = x - x^3/3! + x^5/5! - x^7/7! + ...
cos(x) = 1 - x^2/2! + x^4/4! - x^6/6! + ...

These were rediscovered in Europe by Isaac Newton around 1669.

---

## 3. Yuktibhāṣā: The World's First Calculus Textbook (1530 CE)
Authored in Malayalam by **Jyeshthadeva**, the *Yuktibhāṣā* (*Rationale in the Vernacular*) provided rigorous geometric and algebraic proofs for:
* Derivation of infinite power series
* Differentiation and integration of arc functions
* Term-by-term integration of infinitesimals (dx)
* Rapidly converging error-correction terms (*antya-samskara*)
            """.trimIndent(),
            contentHindi = "केरल के संगमग्राम के माधव ने १४वीं शताब्दी में न्यूटन और लाइबनिज से सदियों पूर्व त्रिकोणमितीय फलनों की अनन्त श्रेणियों का आविष्कार किया...",
            contentMarathi = "केरळच्या माधव यांनी १४ व्या शतकातच कॅल्क्युलसच्या अनंत श्रेणींचा शोध लावला होता...",
            historicalContext = "Developed in medieval Kerala along the Malabar coast. Texts were preserved in palm-leaf manuscripts written in Malayalam and Grantha scripts.",
            keyFigures = listOf("Madhava of Sangamagrama (1340–1425 CE)", "Nilakantha Somayaji (1444–1544 CE)", "Jyeshthadeva (c. 1500–1575 CE)"),
            keyTexts = listOf("Yuktibhasha", "Tantrasamgraha", "Karanapaddhati"),
            sources = listOf(
                SourceCitation(
                    title = "Yuktibhasa of Jyeshthadeva: An Analytical Exposition of Mathematics and Astronomy",
                    authorOrEditor = "K.V. Sarma, M.D. Srinivas, M.S. Sriram",
                    institutionOrPublisher = "Hindustan Book Agency / Springer",
                    publicationYear = "2008",
                    referenceText = "Volume 1: Mathematics - Complete translation and commentary."
                )
            ),
            relatedTopicIds = listOf("lesson_math_103")
        ),
        Lesson(
            id = "lesson_astro_101",
            courseId = "course_astro_1",
            title = "The 27 Nakshatras & The Indian Calendrical System",
            titleHi = "२७ नक्षत्र एवं भारतीय पंचांग व्यवस्था",
            titleMr = "२७ नक्षत्रे आणि भारतीय पंचांग प्रणाली",
            readingTimeMinutes = 7,
            order = 1,
            overview = "An observational guide to the lunar zodiac of 27 Nakshatras, the sidereal solar year, and the five astronomical coordinates of the Panchanga.",
            keyConcepts = listOf(
                "Sidereal vs Tropical Zodiac",
                "Division of the 360° ecliptic into 27 equal arcs (13° 20')",
                "The 5 limbs of the Panchanga: Tithi, Vaara, Nakshatra, Yoga, Karana",
                "Intercalary month (Adhika Masa) synchronization"
            ),
            contentMarkdown = """
# The 27 Nakshatras & The Indian Calendrical Architecture

Unlike Western tropical astrology which anchors on the equinoxes, Indian astronomy (*Jyotiṣa*) operates on a **sidereal framework** (*Nirayana*), calibrating celestial bodies against prominent fixed marker stars (*Yogatārās*).

---

## 1. The 27 Nakshatras (Lunar Mansions)
The Moon completes its apparent orbit across the background stars in approximately 27.32 days. The 360° ecliptic is thus divided into **27 equal arcs of 13° 20'** (800 minutes of arc):
$$27 \times 13^\circ 20' = 360^\circ$$

Each Nakshatra is further subdivided into 4 **Pādas** (quarters) of 3° 20' each ($27 \times 4 = 108$ quarters, matching the sacred count of beads in a Japa Mala).

Major marker stars include:
* **Ashwini:** Alpha Arietis (Hamal)
* **Rohini:** Alpha Tauri (Aldebaran)
* **Chitra:** Alpha Virginis (Spica) - The zero-point calibration star
* **Jyeshtha:** Alpha Scorpii (Antares)
* **Swati:** Alpha Boötis (Arcturus)

---

## 2. The Five Limbs of the Panchanga
The **Pañcāṅga** (पञ्चाङ्ग) constitutes a high-precision observational astronomical calendar tracking five coordinates:

1. **Tithi (Lunar Day):** The time required for the Moon's longitudinal position to advance 12° ahead of the Sun ($360^\circ / 12^\circ = 30$ Tithis in a synodic month).
2. **Vāsara (Day of the Week):** Based on the traditional 7-planet hourly ruler system (Hora).
3. **Nakshatra:** The lunar asterism occupied by the Moon at sunrise.
4. **Yoga:** The angular combination of the Sun's and Moon's longitudes divided by 13° 20'.
5. **Karaṇa:** Half a Tithi (6° elongation between Sun and Moon).

---

## 3. Lunisolar Synchronization: Adhika Māsa
Because 12 lunar synodic months total ~354.36 days while a sidereal solar year is ~365.25 days, a discrepancy of ~11 days accumulates annually. To maintain alignment with seasons, Indian astronomers instituted the **Adhika Māsa** (intercalary month) every ~32.5 months, whenever a lunar month occurs with no solar transit (*Sankranti*).
            """.trimIndent(),
            contentHindi = "भारतीय खगोलशास्त्र में राशि चक्र को २७ नक्षत्रों में विभाजित किया गया है। पंचांग के पाँच अंग तिथि, वार, नक्षत्र, योग और करण हैं...",
            contentMarathi = "भारतीय खगोलशास्त्रात २७ नक्षत्रे आणि पंचांगाचे ५ मुख्य घटक आहेत...",
            historicalContext = "Described in the Rigveda and Atharvaveda, systematically codified in Vedanga Jyotisha of Lagadha (c. 1200–500 BCE) and later Siddhantas.",
            keyFigures = listOf("Lagadha (c. 1000 BCE)", "Varahamihira (505–587 CE)"),
            keyTexts = listOf("Vedanga Jyotisha", "Surya Siddhanta", "Pancha-Siddhantika"),
            sources = listOf(
                SourceCitation(
                    title = "Indian Astronomy: A Source-Book",
                    authorOrEditor = "B.V. Subbarayappa & K.V. Sarma",
                    institutionOrPublisher = "Nehru Centre, Mumbai",
                    publicationYear = "1985",
                    referenceText = "Chapters on Vedic Astronomy and the Nakshatra system."
                )
            ),
            relatedTopicIds = listOf("lesson_astro_102")
        ),
        Lesson(
            id = "lesson_ayur_101",
            courseId = "course_ayur_1",
            title = "Tridosha Physiology & The Holistic Health Model",
            titleHi = "त्रिदोष शरीरक्रिया एवं समग्र स्वास्थ्य",
            titleMr = "त्रिदोष सिद्धांत आणि समग्र आरोग्य",
            readingTimeMinutes = 7,
            order = 1,
            overview = "Understand the Tridosha paradigm—Vata, Pitta, and Kapha—as dynamic biological regulators of homeostasis, digestion, and neuro-endocrine balance.",
            keyConcepts = listOf(
                "Pancha Mahabhuta (The Five Elements)",
                "Vata (Kinetic), Pitta (Metabolic), Kapha (Structural)",
                "Prakriti (Genetic constitution) vs Vikriti (Current imbalance)",
                "Agni (Digestive & metabolic fire)"
            ),
            contentMarkdown = """
# Tridosha Physiology: Dynamic Biological Balance

Ayurveda defines health (*Svāsthya*) not as the mere absence of disease, but as psycho-physiological equilibrium:

> **"समदोषः समाग्निश्च समधातुमलक्रियः।**
> **प्रसन्नात्मेन्द्रियमनाः स्वस्थ इत्यभिधीयते॥"**
> 
> *"He whose doshas are in balance, whose digestive fire (agni) is balanced, whose tissues (dhatus) and excretions (malas) function properly, and whose soul, senses, and mind are delightfully serene, is called healthy."*
> — *Suśruta Saṃhitā, Sūtrasthāna 15.41*

---

## 1. The Pancha Mahabhuta Foundation
All physical matter consists of five constituent states of energy and matter:
1. **Ākāśa (Space/Ether):** Medium of non-resistance and vibration.
2. **Vāyu (Air/Gaseous):** Mobility and kinetic momentum.
3. **Agni/Tejas (Fire/Transformation):** Temperature, digestion, metabolism.
4. **Jala/Āpas (Water/Liquid):** Cohesion, lubrication, hydration.
5. **Pṛthvī (Earth/Solid):** Density, skeletal structure, mass.

---

## 2. The Three Functional Principles (Tridoṣa)
* **Vāta (Space + Air):** Governs movement, nerve impulse transmission, respiration, circulation, and peristalsis.
  * Qualities: Dry, light, cold, rough, subtle, mobile.
* **Pitta (Fire + Water):** Governs enzymatic digestion, thermogenesis, cellular metabolism, biochemical transformation, and visual perception.
  * Qualities: Hot, sharp, liquid, slightly oily, sour/pungent.
* **Kapha (Water + Earth):** Governs anabolism, biological lubrication, immune resilience (*Ojas*), bone/tissue cohesion, and structural integrity.
  * Qualities: Heavy, slow, cool, oily, smooth, stable.

---

## 3. Prakriti vs. Vikriti
Every human possesses an individualized baseline genetic/phenotypic balance established at conception called **Prakṛti**. Acquired imbalances resulting from diet, circadian disruption, stress, and environmental shifts are termed **Vikṛti**. Clinical treatment restores harmony through targeted herbs, nutrition, and lifestyle modification.
            """.trimIndent(),
            contentHindi = "आयुर्वेद में स्वास्थ्य की परिभाषा अत्यंत विशद है। वात, पित्त और कफ का संतुलन ही स्वास्थ्य का आधार है...",
            contentMarathi = "आयुर्वेदात वात, पित्त आणि कफ या त्रिदोषांचे संतुलन म्हणजेच उत्तम आरोग्य होय...",
            historicalContext = "Codified in the classical medical compendiums (Charaka Samhita and Sushruta Samhita) between 300 BCE and 300 CE.",
            keyFigures = listOf("Acharya Charaka", "Acharya Sushruta", "Vagbhata"),
            keyTexts = listOf("Charaka Samhita", "Sushruta Samhita", "Ashtanga Hridaya"),
            sources = listOf(
                SourceCitation(
                    title = "Charaka Samhita (Critical Text with English Translation)",
                    authorOrEditor = "P.V. Sharma",
                    institutionOrPublisher = "Chaukhambha Orientalia",
                    publicationYear = "2000",
                    referenceText = "Sutrasthana Chapters 1 & 11."
                )
            ),
            relatedTopicIds = listOf("lesson_ayur_102")
        ),
        Lesson(
            id = "lesson_ayur_102",
            courseId = "course_ayur_1",
            title = "Sushruta: Ancient Surgery, Rhinoplasty & Instruments",
            titleHi = "सुश्रुत: प्राचीन शल्यक्रिया, राइनोप्लास्टी एवं उपकरण",
            titleMr = "सुश्रुत: शस्त्रक्रिया आणि प्लास्टिक सर्जरी",
            readingTimeMinutes = 7,
            order = 2,
            overview = "Acharya Sushruta's classical documentation of over 120 surgical instruments, pedicled cheek-flap rhinoplasty, cataract extraction, and suture materials.",
            keyConcepts = listOf(
                "Sushruta Samhita: The surgeon's guide",
                "Rhinoplasty (forehead and cheek flap reconstruction)",
                "Yantras (blunt) and Shastras (sharp) instruments",
                "Pre-operative and post-operative asepsis"
            ),
            contentMarkdown = """
# Acharya Sushruta: Father of Surgery & Plastic Surgery

Writing in Kashi (modern Varanasi) around the 6th century BCE, **Acharya Suśruta** composed the *Suśruta Saṃhitā*, the most sophisticated surgical textbook of antiquity.

---

## 1. Pioneer of Plastic Surgery (Pedicled Rhinoplasty)
In ancient India, nasal amputation was a frequent punishment inflicted during warfare or judicial decrees. In *Sūtrasthāna, Chapter 16*, Sushruta detailed the reconstructive method that laid the foundation for modern plastic surgery:

1. Measuring the missing portion of the nose with a leaf.
2. Cutting a corresponding pedicled skin flap from the adjacent cheek or forehead (leaving a vascular pedicle attached for blood supply).
3. Scarifying the nasal stump and carefully suturing the living tissue.
4. Inserting two hollow castor-oil plant or reed tubes (*Eranda*) to preserve airway patency during healing.
5. Dusting with astringent hemostatic herbal powders (liquorice, red sandalwood).

When British military surgeons observed Indian Vaidyas performing this exact forehead flap procedure in Pune in 1793, they published it in the *Gentleman's Magazine* (1794), directly giving rise to modern reconstructive plastic surgery in the West.

---

## 2. Surgical Instrumentation: 121 Tools
Sushruta categorized surgical tools into two broad families:
* **Yantras (101 Blunt Instruments):** Forceps, speculums, trocars, catheters, and retractors modeled on the jaws and beaks of predatory birds and animals (e.g., lion-jaw forceps, heron-beak forceps).
* **Śastras (20 Sharp Instruments):** Scalpels, lancets, trocars, bone saws, and curved suture needles made of tempered carbon steel.
            """.trimIndent(),
            contentHindi = "सुश्रुत ने काशी में शल्यक्रिया का महान ग्रंथ लिखा। उन्होंने राइनोप्लास्टी और १२१ शल्य उपकरणों का वर्णन किया...",
            contentMarathi = "सुश्रुत यांनी प्राचीन भारतात प्लास्टिक सर्जरी आणि मोतीबिंदू शस्त्रक्रियेचे तंत्रज्ञान विकसित केले...",
            historicalContext = "Developed in the ancient kingdom of Kashi around the 6th century BCE. Taught via anatomical dissection using submerged water immersion techniques.",
            keyFigures = listOf("Acharya Sushruta"),
            keyTexts = listOf("Sushruta Samhita"),
            sources = listOf(
                SourceCitation(
                    title = "Sushruta Samhita: A Scientific Synopsis",
                    authorOrEditor = "P. Ray, H.N. Gupta, M. Roy",
                    institutionOrPublisher = "Indian National Science Academy",
                    publicationYear = "1980",
                    referenceText = "Chapters on surgical techniques and instrumentation."
                )
            ),
            relatedTopicIds = listOf("lesson_ayur_101")
        ),
        Lesson(
            id = "lesson_phil_101",
            courseId = "course_phil_1",
            title = "Nyaya Logic: Epistemology & The Four Pramanas",
            titleHi = "न्याय दर्शन: ज्ञानमीमांसा एवं चार प्रमाण",
            titleMr = "न्याय दर्शन: प्रमाणशास्त्र आणि तर्क",
            readingTimeMinutes = 8,
            order = 1,
            overview = "Explore Aksapada Gautama's Nyaya school of epistemology, which defined the criteria for true knowledge and established a formal 5-step syllogism.",
            keyConcepts = listOf(
                "Pramana (Valid means of knowledge)",
                "Pratyaksha (Direct Perception)",
                "Anumana (Logical Inference) and the 5-step syllogism",
                "Upamana (Analogy) and Shabda (Reliable Testimony)"
            ),
            contentMarkdown = """
# Nyaya Logic: The Science of Rational Inquiry

Founded by sage **Akṣapāda Gautama** in the *Nyāya Sūtras* (c. 2nd century BCE), the Nyaya school provides the epistemic operating system for classical Indian debate (*Vāda*).

---

## 1. The Four Valid Means of Knowledge (Pramāṇas)
Nyaya asserts that accurate cognition (*Pramā*) requires valid epistemic tools (*Pramāṇa*):

1. **Pratyakṣa (Perception):** Direct non-erroneous cognition produced through sensory contact with external objects.
2. **Anumāna (Inference):** Deducing an unobserved fact from an observed sign (*Linga*) based on universal concomitance (*Vyāpti*).
   * Example: *Where there is smoke, there is fire (as in a kitchen hearth).*
3. **Upamāna (Analogy / Comparison):** Gaining knowledge of an unfamiliar entity through perceived similarity with a previously known entity.
4. **Śabda (Verbal Testimony):** Testimony delivered by an *Āpta*—a trustworthy, competent, and unbiased authority.

---

## 2. The Nyaya Five-Step Syllogism (Pañcāvayava)
Unlike Aristotle's 3-step deductive syllogism, Nyaya established a 5-member logical argument that integrates **deduction with empirical induction**:

1. **Pratijñā (Proposition):** *The hill has fire.*
2. **Hetu (Reason):** *Because it has smoke.*
3. **Udāharaṇa (Universal Rule with Example):** *Wherever there is smoke, there is fire, as in a kitchen hearth; and unlike a water lake.*
4. **Upanaya (Application):** *This hill has smoke which is invariably associated with fire.*
5. **Nigamana (Conclusion):** *Therefore, this hill has fire.*
            """.trimIndent(),
            contentHindi = "न्याय दर्शन के प्रवर्तक महर्षि गौतम हैं। इसमें यथार्थ ज्ञान के चार प्रमाण माने गए हैं: प्रत्यक्ष, अनुमान, उपमान और शब्द...",
            contentMarathi = "महर्षी गौतम यांनी न्याय दर्शनाची रचना केली. यात ज्ञान मिळवण्याचे चार मार्ग सांगितले आहेत...",
            historicalContext = "Developed across ancient Mithila and Varanasi. Later revived in Navya-Nyaya (New Logic) by Gangeśa Upādhyāya in the 12th century CE.",
            keyFigures = listOf("Aksapada Gautama", "Vatsyayana", "Gangesha Upadhyaya"),
            keyTexts = listOf("Nyaya Sutras", "Nyaya Bhashya", "Tattva Chintamani"),
            sources = listOf(
                SourceCitation(
                    title = "Indian Logic and Atomism: An Exposition of the Nyaya and Vaisheshika Systems",
                    authorOrEditor = "Arthur Berriedale Keith",
                    institutionOrPublisher = "Clarendon Press, Oxford",
                    publicationYear = "1921",
                    referenceText = "Chapters 2 & 3 on Nyaya Epistemology."
                )
            ),
            relatedTopicIds = listOf("course_phil_1")
        ),
        Lesson(
            id = "lesson_metal_101",
            courseId = "course_metal_1",
            title = "The Delhi Iron Pillar: Rust-Resistant Nanotechnology",
            titleHi = "दिल्ली का लौह स्तंभ: जंग-प्रतिरोधी धातु विज्ञान",
            titleMr = "दिल्लीचा लोहस्तंभ: प्राचीन धातुशास्त्र",
            readingTimeMinutes = 7,
            order = 1,
            overview = "The metallurgical mystery of the 1,600-year-old rustless Iron Pillar of Delhi: forge-welding, high phosphorus chemistry, and the protective misawite passive layer.",
            keyConcepts = listOf(
                "Gupta-era forge welding (Chandra King inscription)",
                "Weight of over 6 tonnes, 98% pure wrought iron",
                "High phosphorus and absence of sulfur/manganese",
                "Formation of protective amorphous iron hydrogen phosphate (Misawite)"
            ),
            contentMarkdown = """
# The Delhi Iron Pillar: Metallurgy of the Gupta Era

Standing in the Qutb Complex in Mehrauli, New Delhi, the **Iron Pillar** was erected during the reign of Chandragupta II Vikramaditya (c. 375–415 CE). Despite standing exposed to monsoon rains, scorching heat, and humid air for over **1,600 years**, it has remained remarkably corrosion-resistant.

---

## 1. Physical Specifications
* **Height:** 7.21 metres (including subterranean base)
* **Diameter:** 48 cm at base tapering to 29 cm at top
* **Weight:** Exceeds 6,000 kilograms (6 tonnes)
* **Composition:** Wrought iron (99.72% Fe)

---

## 2. Ancient Forge-Welding Engineering
The pillar was not cast in a molten foundry. Instead, ancient Indian metallurgists extracted iron sponge in charcoal-fired bloomery furnaces and incrementally **hammer-welded** hundreds of hot semi-solid lumps weighing 20–30 kg each. The outer surface was then hot-burnished and coated.

---

## 3. The Corrosion-Resistance Mechanism
Metallurgical investigations spearheaded by Prof. R. Balasubramaniam (IIT Kanpur) revealed the chemical secret:

1. **High Phosphorus Content:** Ancient Indian ironsmiths utilized phosphorus-rich local iron ores and did not add lime during smelting. The iron contains **0.25% phosphorus** (compared to modern steel's < 0.05%).
2. **Misawite Passive Shield:** The phosphorus acts as a catalytic agent at the metal-scale interface. Over several decades of alternating wet and dry cycles, it formed a microscopic, non-porous protective film (approx. 50 microns thick) of **amorphous iron hydrogen phosphate hydrate** ($\delta\text{-FeOOH}$, known as *Misawite*).
3. This passive barrier completely seals the underlying core iron from atmospheric oxygen and moisture.
            """.trimIndent(),
            contentHindi = "दिल्ली का लौह स्तंभ गुप्त सम्राट चंद्रगुप्त विक्रमादित्य के काल का है। इसमें फास्फोरस की विशेष मात्रा होने से मिसावाइट की परत बनी...",
            contentMarathi = "१६०० वर्षांनंतरही गंज न चढलेला दिल्लीचा लोहस्तंभ हे प्राचीन भारतीय धातुशास्त्राचे अद्वितीय उदाहरण आहे...",
            historicalContext = "Cast and forge-welded in the late 4th or early 5th century CE, originally installed atop Udayagiri/Vishnupadagiri hill near Vidisha (Madhya Pradesh).",
            keyFigures = listOf("Chandragupta II Vikramaditya", "Prof. R. Balasubramaniam (IIT Kanpur researcher)"),
            keyTexts = listOf("Mehrauli Inscription of King Chandra", "Rasaratnasamuchchaya"),
            sources = listOf(
                SourceCitation(
                    title = "Delhi Iron Pillar: New Insights",
                    authorOrEditor = "R. Balasubramaniam",
                    institutionOrPublisher = "Indian Institute of Advanced Study & Aryan Books",
                    publicationYear = "2002",
                    referenceText = "Complete material science analysis of corrosion resistance."
                )
            ),
            relatedTopicIds = listOf("course_metal_1")
        )
    )

    val quizQuestions = listOf(
        QuizQuestion(
            id = "q_1",
            lessonId = "lesson_math_101",
            courseId = "course_math_1",
            domainId = "math",
            questionText = "Which ancient Indian text contains the earliest documented statement of the theorem relating the diagonal of a rectangle to its sides?",
            questionTextHi = "किस प्राचीन भारतीय ग्रंथ में आयत के विकर्ण और उसकी भुजाओं के संबंध के प्रमेय का प्राचीनतम उल्लेख है?",
            questionTextMr = "काटकोन त्रिकोणाचा कर्ण सिद्धांत कोणत्या प्राचीन भारतीय ग्रंथात सर्वप्रथम आला आहे?",
            type = QuestionType.MCQ,
            options = listOf("Baudhayana Sulba Sutra", "Aryabhatiya", "Lilavati", "Surya Siddhanta"),
            correctOptionIndex = 0,
            explanation = "Baudhayana Sulba Sutra (1.48) explicitly states that the rope stretched along the diagonal of a rectangle produces the area that the sides produce together.",
            explanationHi = "बौधायन शुल्ब सूत्र (१.४८) में स्पष्ट उल्लेख है कि आयत के विकर्ण की रस्सी से बना वर्ग उसकी दोनों भुजाओं के वर्गों के योग के बराबर होता है।",
            explanationMr = "बौधायन शुल्ब सूत्रात (१.४८) पायथागोरसच्या आधीच काटकोन त्रिकोणाच्या कर्णाचा सिद्धांत नोंदवला गेला आहे.",
            sourceRef = "Baudhayana Sulba Sutra 1.48; G.G. Joseph, 'Crest of the Peacock'",
            difficulty = "Easy"
        ),
        QuizQuestion(
            id = "q_2",
            lessonId = "lesson_math_101",
            courseId = "course_math_1",
            domainId = "math",
            questionText = "What was the rational fraction approximation for the square root of 2 given in the Sulba Sutras?",
            questionTextHi = "शुल्ब सूत्रों में √२ (वर्गमूल दो) का कौन सा सन्निकट परिमेय मान दिया गया है?",
            questionTextMr = "शुल्ब सूत्रांमध्ये √२ चे कोणते अचूक मूल्य दिले आहे?",
            type = QuestionType.MCQ,
            options = listOf("577 / 408", "355 / 113", "22 / 7", "99 / 70"),
            correctOptionIndex = 0,
            explanation = "Baudhayana and Apastamba gave 1 + 1/3 + 1/(3*4) - 1/(3*4*34) = 577/408 ≈ 1.4142156, accurate to five decimal places.",
            explanationHi = "१ + १/३ + १/१२ - १/४०८ = ५७७/४०८ ≈ १.४१४२१५६, जो दशमलव के ५ स्थानों तक शुद्ध है।",
            explanationMr = "५७७ / ४०८ हे सूत्र बौधायन यांनी दिले, जे दशांश ५ अंकांपर्यंत अचूक आहे.",
            sourceRef = "Apastamba Sulba Sutra 1.6",
            difficulty = "Medium"
        ),
        QuizQuestion(
            id = "q_3",
            lessonId = "lesson_math_102",
            courseId = "course_math_1",
            domainId = "math",
            questionText = "Who was the first mathematician in world history to formalize algebraic arithmetic operations involving zero and negative numbers?",
            questionTextHi = "शून्य एवं ऋणात्मक संख्याओं के बीजगणितीय नियमों को औपचारिक रूप से प्रतिपादित करने वाले विश्व के प्रथम गणितज्ञ कौन थे?",
            questionTextMr = "शून्य आणि ऋण संख्यांचे अंकगणितीय नियम सर्वप्रथम कोणी मांडले?",
            type = QuestionType.MCQ,
            options = listOf("Brahmagupta", "Aryabhata", "Varahamihira", "Bhaskara II"),
            correctOptionIndex = 0,
            explanation = "Brahmagupta in his Brahmasphutasiddhanta (628 CE, Chapter 18) formulated explicit rules for addition, subtraction, and multiplication involving zero.",
            explanationHi = "ब्रह्मगुप्त ने ६२८ ईस्वी में ब्राह्मस्फुटसिद्धान्त के १८वें अध्याय में शून्य एवं ऋण राशियों के नियम दिए।",
            explanationMr = "इ.स. ६२८ मध्ये ब्रह्मगुप्त यांनी ब्राह्मस्फुटसिद्धान्त ग्रंथात शून्याचे नियम दिले.",
            sourceRef = "Brahmasphutasiddhanta, Chapter 18",
            difficulty = "Easy"
        ),
        QuizQuestion(
            id = "q_4",
            lessonId = "lesson_math_103",
            courseId = "course_math_1",
            domainId = "math",
            questionText = "True or False: Aryabhata explicitly marked his approximation of Pi (3.1416) with the word 'asanna', indicating it is an approximation rather than an exact rational number.",
            questionTextHi = "सत्य या असत्य: आर्यभट्ट ने पाई के मान ३.१४१६ के साथ 'आसन्न' शब्द का प्रयोग किया, जिसका अर्थ सन्निकट (अपरिमेय) है।",
            questionTextMr = "सत्य की असत्य: आर्यभटांनी पायच्या मूल्याला 'आसन्न' (अंदाजे) म्हटले होते.",
            type = QuestionType.TRUE_FALSE,
            options = listOf("True", "False"),
            correctOptionIndex = 0,
            explanation = "True. In Aryabhatiya (Ganitapada verse 10), Aryabhata used 'asanno vritta-parinahah', demonstrating his awareness that pi cannot be written as an exact rational ratio.",
            explanationHi = "सत्य। आर्यभटीय गणितपाद श्लोक १० में 'आसन्न' शब्द प्रयुक्त है।",
            explanationMr = "सत्य. आर्यभटांनी 'आसन्न' हा शब्द वापरून अपरिमेयतेची जाणीव दर्शवली.",
            sourceRef = "Aryabhatiya, Ganitapada, verse 10",
            difficulty = "Easy"
        ),
        QuizQuestion(
            id = "q_5",
            lessonId = "lesson_math_104",
            courseId = "course_math_1",
            domainId = "math",
            questionText = "Which Malayalam text authored by Jyeshthadeva in 1530 CE is recognized as the world's first comprehensive calculus textbook?",
            questionTextHi = "१५३० ईस्वी में ज्येष्ठदेव द्वारा रचित किस ग्रंथ को विश्व का प्रथम कलन (कैलकुलस) पाठ्यपुस्तक माना जाता है?",
            questionTextMr = "१५३० मधील ज्येष्ठदेव यांचा कोणता मल्याळम ग्रंथ जगातील पहिले कॅल्क्युलसचे पुस्तक मानला जातो?",
            type = QuestionType.MCQ,
            options = listOf("Yuktibhasha", "Tantrasamgraha", "Lilavati", "Karanapaddhati"),
            correctOptionIndex = 0,
            explanation = "Yuktibhasha (Rationale in the Vernacular) by Jyeshthadeva systematically derived infinite series for sine, cosine, arctangent, and differential techniques.",
            explanationHi = "युक्तिभाषा में अनन्त श्रेणियों, अवकलन एवं समाकलन के आधारभूत प्रमाण दिए गए हैं।",
            explanationMr = "युक्तिभाषा या ग्रंथात कॅल्क्युलसच्या सिद्धांतांचे सविस्तर पुरावे दिले आहेत.",
            sourceRef = "Yuktibhasha of Jyeshthadeva, Springer 2008",
            difficulty = "Medium"
        ),
        QuizQuestion(
            id = "q_6",
            lessonId = "lesson_ayur_102",
            courseId = "course_ayur_1",
            domainId = "ayurveda",
            questionText = "Which surgical procedure described by Acharya Sushruta directly influenced the development of modern reconstructive plastic surgery?",
            questionTextHi = "आचार्य सुश्रुत द्वारा वर्णित किस शल्य प्रक्रिया ने आधुनिक प्लास्टिक सर्जरी के विकास को सीधे प्रेरित किया?",
            questionTextMr = "आचार्य सुश्रुत यांच्या कोणत्या शस्त्रक्रियेने आधुनिक प्लास्टिक सर्जरीचा पाया घातला?",
            type = QuestionType.MCQ,
            options = listOf("Pedicled cheek/forehead flap Rhinoplasty", "Trepanation", "Appendectomy", "Skin grafting for burns"),
            correctOptionIndex = 0,
            explanation = "Sushruta's cheek and forehead pedicled flap rhinoplasty was observed in Pune in 1793 and published in the British medical literature, launching modern plastic surgery.",
            explanationHi = "सुश्रुत की कपोल/ललाट फ्लैप राइनोप्लास्टी विधि से ही आधुनिक प्लास्टिक सर्जरी का सूत्रपात हुआ।",
            explanationMr = "सुश्रुत यांची कपाळावरील त्वचेचा वापर करून नाक पूर्ववत करण्याची राइनोप्लास्टी पद्धत जागतिक स्तरावर प्रसिद्ध झाली.",
            sourceRef = "Sushruta Samhita, Sutrasthana 16; Gentleman's Magazine (1794)",
            difficulty = "Easy"
        ),
        QuizQuestion(
            id = "q_7",
            lessonId = "lesson_phil_101",
            courseId = "course_phil_1",
            domainId = "philosophy",
            questionText = "How many valid means of knowledge (Pramanas) are accepted by the Nyaya school of Indian philosophy?",
            questionTextHi = "भारतीय दर्शन के न्याय सम्प्रदाय द्वारा कितने प्रमाण स्वीकार किए गए हैं?",
            questionTextMr = "न्याय तत्त्वज्ञानात किती प्रमाणे (ज्ञानाची साधने) मानली आहेत?",
            type = QuestionType.MCQ,
            options = listOf("Four: Pratyaksha, Anumana, Upamana, Shabda", "Two: Pratyaksha and Anumana", "Six: Including Arthapatti and Anupalabdhi", "One: Only Pratyaksha"),
            correctOptionIndex = 0,
            explanation = "Nyaya accepts exactly four Pramanas: Perception (Pratyaksha), Inference (Anumana), Comparison (Upamana), and Word/Testimony (Shabda).",
            explanationHi = "न्याय दर्शन में चार प्रमाण हैं: प्रत्यक्ष, अनुमान, उपमान और शब्द।",
            explanationMr = "न्याय दर्शन चार प्रमाणे मानते: प्रत्यक्ष, अनुमान, उपमान आणि शब्द.",
            sourceRef = "Nyaya Sutras 1.1.3",
            difficulty = "Easy"
        ),
        QuizQuestion(
            id = "q_8",
            lessonId = "lesson_metal_101",
            courseId = "course_metal_1",
            domainId = "metallurgy",
            questionText = "What compound forms the protective microscopic passive film responsible for the 1,600-year rust resistance of the Delhi Iron Pillar?",
            questionTextHi = "दिल्ली के लौह स्तंभ को १६०० वर्षों से जंग से बचाने वाली सूक्ष्म सुरक्षात्मक परत किस यौगिक से बनी है?",
            questionTextMr = "दिल्लीच्या लोहस्तंभावर गंज न चढण्यासाठी कोणत्या घटकाचा संरक्षक थर कारणीभूत आहे?",
            type = QuestionType.MCQ,
            options = listOf("Amorphous iron hydrogen phosphate (Misawite)", "Zinc oxide galvanization", "Titanium dioxide coating", "Chromium oxide plating"),
            correctOptionIndex = 0,
            explanation = "Due to high phosphorus (0.25%) in the wrought iron, an amorphous iron hydrogen phosphate hydrate (Misawite) passive layer formed, sealing it from oxidation.",
            explanationHi = "लौह स्तंभ के उच्च फास्फोरस के कारण मिसावाइट (आयरन हाइड्रोजन फॉस्फेट) की सुरक्षात्मक परत बनी।",
            explanationMr = "फॉस्फरसच्या प्रमाणामुळे तयार झालेला मिसावाइटचा थर लोखंडाला ऑक्सिजनपासून सुरक्षित ठेवतो.",
            sourceRef = "Prof. R. Balasubramaniam, IIT Kanpur Research",
            difficulty = "Hard"
        )
    )

    val flashcards = listOf(
        Flashcard(
            id = "fc_1",
            lessonId = "lesson_math_101",
            domainId = "math",
            frontText = "Baudhayana Sulba Sutra (1.48)",
            frontTextHi = "बौधायन शुल्ब सूत्र (१.४८)",
            frontTextMr = "बौधायन शुल्ब सूत्र (१.४८)",
            backText = "Stipulated the Pythagorean geometric relation: The diagonal rope of a rectangle produces the sum of areas made by its horizontal and vertical sides.",
            backTextHi = "आयत के विकर्ण की रस्सी से बना वर्ग उसकी दोनों भुजाओं के वर्गों के योग के बराबर होता है।",
            backTextMr = "आयाताच्या कर्णावर काढलेला चौरस हा त्याच्या दोन्ही बाजूंच्या चौरसांच्या बेरजेइतका असतो.",
            category = "Geometry",
            sourceRef = "Sulba Sutras (c. 800 BCE)",
            masteryLevel = 1
        ),
        Flashcard(
            id = "fc_2",
            lessonId = "lesson_math_102",
            domainId = "math",
            frontText = "Brahmasphutasiddhanta (628 CE)",
            frontTextHi = "ब्राह्मस्फुटसिद्धान्त (६२८ ईस्वी)",
            frontTextMr = "ब्राह्मस्फुटसिद्धान्त (इ.स. ६२८)",
            backText = "Composed by Brahmagupta; formulated the world's first formal rules of arithmetic for zero (Shunya) and negative numbers (Kshaya/Rina).",
            backTextHi = "ब्रह्मगुप्त द्वारा रचित; विश्व में प्रथम बार शून्य और ऋणात्मक संख्याओं के जोड़, घटाव एवं गुणा के नियम दिए।",
            backTextMr = "ब्रह्मगुप्तांनी शून्य व ऋण संख्यांच्या अंकगणिताचे नियम मांडले.",
            category = "Arithmetic",
            sourceRef = "Brahmasphutasiddhanta Ch. 18",
            masteryLevel = 2
        ),
        Flashcard(
            id = "fc_3",
            lessonId = "lesson_math_103",
            domainId = "math",
            frontText = "Aryabhata's Value of Pi (π)",
            frontTextHi = "आर्यभट्ट द्वारा पाई (π) का मान",
            frontTextMr = "आर्यभटांनी दिलेले पायचे मूल्य",
            backText = "62,832 / 20,000 = 3.1416. Stated in Aryabhatiya (499 CE) and designated as 'asanna' (approximative).",
            backTextHi = "६२,८३२ / २०,००० = ३.१४१६। आर्यभटीय में इसे 'आसन्न' (सन्निकट) कहा गया।",
            backTextMr = "६२,८३२ / २०,००० = ३.१४१६, जे 'आसन्न' म्हणजेच अंदाजे मूल्य म्हणून नोंदवले.",
            category = "Geometry",
            sourceRef = "Aryabhatiya, Ganitapada 10",
            masteryLevel = 0
        ),
        Flashcard(
            id = "fc_4",
            lessonId = "lesson_math_104",
            domainId = "math",
            frontText = "Madhava Series for Arctangent",
            frontTextHi = "आर्कटेंजेंट की माधव श्रेणी",
            frontTextMr = "माधव यांची आर्कटँजंट श्रेणी",
            backText = "π/4 = 1 - 1/3 + 1/5 - 1/7 + ... Derived by Madhava of Sangamagrama c. 1400 CE, 300 years before Leibniz and Gregory.",
            backTextHi = "π/४ = १ - १/३ + १/५ - १/७ + ... लाइबनिज से ३०० वर्ष पूर्व केरल के माधव द्वारा खोजी गई।",
            backTextMr = "लायबनिझच्या ३०० वर्षे आधी माधव यांनी ही अनंत श्रेणी शोधली.",
            category = "Calculus",
            sourceRef = "Yuktibhasha & Tantrasamgraha",
            masteryLevel = 1
        ),
        Flashcard(
            id = "fc_5",
            lessonId = "lesson_ayur_101",
            domainId = "ayurveda",
            frontText = "The Tridoshas of Ayurveda",
            frontTextHi = "आयुर्वेद के त्रिदोष",
            frontTextMr = "आयुर्वेदातील त्रिदोष",
            backText = "Vata (Kinetic/Nervous), Pitta (Metabolic/Thermal), Kapha (Structural/Fluids). Health is dynamic balance (Samadosha).",
            backTextHi = "वात (गति), पित्त (पाचन/ऊष्मा), कफ (संरचना/स्निग्धता)। इनका संतुलन ही स्वास्थ्य है।",
            backTextMr = "वात, पित्त आणि कफ - या तिन्हींचे संतुलन म्हणजेच निरोगी शरीर.",
            category = "Physiology",
            sourceRef = "Charaka Samhita Sutrasthana 1",
            masteryLevel = 2
        ),
        Flashcard(
            id = "fc_6",
            lessonId = "lesson_ayur_102",
            domainId = "ayurveda",
            frontText = "Sushruta's Rhinoplasty",
            frontTextHi = "सुश्रुत की राइनोप्लास्टी विधि",
            frontTextMr = "सुश्रुत यांची राइनोप्लास्टी",
            backText = "Forehead/cheek pedicled living flap technique for nasal reconstruction, described in Sushruta Samhita (6th c. BCE).",
            backTextHi = "ललाट अथवा कपोल से जीवित त्वचा फ्लैप द्वारा नासिका पुनर्निर्माण की प्राचीन विधि।",
            backTextMr = "कपाळावरील त्वचेचा वापर करून नाक दुरुस्ती करण्याची जगातील पहिली प्लास्टिक सर्जरी.",
            category = "Surgery",
            sourceRef = "Sushruta Samhita Sutrasthana 16",
            masteryLevel = 1
        ),
        Flashcard(
            id = "fc_7",
            lessonId = "lesson_phil_101",
            domainId = "philosophy",
            frontText = "Pramanas in Nyaya",
            frontTextHi = "न्याय दर्शन के चार प्रमाण",
            frontTextMr = "न्याय दर्शनातील चार प्रमाणे",
            backText = "Pratyaksha (Direct Perception), Anumana (Inference), Upamana (Analogy), Shabda (Authoritative Testimony).",
            backTextHi = "प्रत्यक्ष, अनुमान, उपमान और आप्त शब्द।",
            backTextMr = "प्रत्यक्ष, अनुमान, उपमान आणि शब्द ही चार प्रमाणे.",
            category = "Logic",
            sourceRef = "Nyaya Sutras 1.1.3",
            masteryLevel = 0
        ),
        Flashcard(
            id = "fc_8",
            lessonId = "lesson_metal_101",
            domainId = "metallurgy",
            frontText = "Delhi Iron Pillar Composition",
            frontTextHi = "दिल्ली लौह स्तंभ का संघटन",
            frontTextMr = "दिल्ली लोहस्तंभाचे रासायनिक संघटन",
            backText = "99.72% wrought iron with 0.25% high phosphorus. Forge-welded in 4th c. CE; forms protective Misawite barrier against rust.",
            backTextHi = "९९.७२% शुद्ध पिटवां लोहा एवं ०.२५% फास्फोरस। जंग प्रतिरोधी मिसावाइट परत।",
            backTextMr = "९९.७२% शुद्ध लोखंड आणि ०.२५% फॉस्फरस, ज्यामुळे मिसावाइटचा थर बनतो.",
            category = "Metallurgy",
            sourceRef = "IIT Kanpur Metallurgy Labs",
            masteryLevel = 1
        )
    )

    val timelineEvents = listOf(
        TimelineEvent(
            id = "time_1",
            period = "c. 2600–1900 BCE",
            eraName = "Indus-Saraswati Era",
            title = "Urban Planning, Standardization & Hydraulic Engineering",
            titleHi = "नगर नियोजन, मानकीकरण एवं जल स्थापत्य",
            description = "At Mohenjo-daro, Harappa, and Dholavira, excavations reveal grid street layouts, standardized kiln-baked brick dimensions (1:2:4 ratio), sophisticated subterranean covered drainage, and stone-cut storm-water reservoir networks.",
            domain = "Architecture & Water Management",
            scholars = listOf("Harappan Master Builders & Guilds"),
            primaryTexts = listOf("Archaeological Remains (Dholavira, Lothal dockyard)"),
            sourceCitation = "Archaeological Survey of India (ASI) Reports"
        ),
        TimelineEvent(
            id = "time_2",
            period = "c. 800–500 BCE",
            eraName = "Late Vedic & Sulba Period",
            title = "Sulba Sutras: Geometry of Altars & Baudhayana Theorem",
            titleHi = "शुल्ब सूत्र: यज्ञवेदी ज्यामिति एवं बौधायन प्रमेय",
            description = "Baudhayana, Apastamba, and Katyayana articulate exact geometric transformation rules, formulate the Pythagorean relationship for rectangles, and calculate the square root of 2 to 5 decimal digits.",
            domain = "Indian Mathematics",
            scholars = listOf("Baudhayana", "Apastamba", "Manava", "Katyayana"),
            primaryTexts = listOf("Baudhayana Sulba Sutra", "Apastamba Sulba Sutra"),
            sourceCitation = "G.G. Joseph, 'Crest of the Peacock'"
        ),
        TimelineEvent(
            id = "time_3",
            period = "c. 6th–5th Century BCE",
            eraName = "Classical Foundational Era",
            title = "Sushruta's Surgery & Panini's Formal Grammar",
            titleHi = "सुश्रुत की शल्य चिकित्सा एवं पाणिनि का अष्टाध्यायी व्याकरण",
            description = "In Kashi, Sushruta documents 121 surgical instruments and plastic surgery (rhinoplasty). Simultaneously in Gandhara, Panini authors the Ashtadhyayi: 4,000 algorithmic rules forming the world's first formal generative grammar.",
            domain = "Ayurveda & Linguistics",
            scholars = listOf("Acharya Sushruta", "Acharya Panini"),
            primaryTexts = listOf("Sushruta Samhita", "Ashtadhyayi"),
            sourceCitation = "Chaukhambha Orientalia & Springer Linguistics"
        ),
        TimelineEvent(
            id = "time_4",
            period = "c. 3rd Century BCE",
            eraName = "Mauryan Era",
            title = "Kautilya's Arthashastra & Pingala's Combinatorics",
            titleHi = "कौटिल्य का अर्थशास्त्र एवं पिंगल का छन्दःशास्त्र",
            description = "Chanakya (Kautilya) compiles the encyclopedic Arthashastra on statecraft, political economy, and intelligence. Pingala develops binary numerical sequences, the Meru Prastara (Pascal's triangle), and early zero markers.",
            domain = "Polity & Mathematics",
            scholars = listOf("Kautilya (Chanakya)", "Pingala"),
            primaryTexts = listOf("Arthashastra", "Chandahsutra"),
            sourceCitation = "R.P. Kangle, 'The Kautiliya Arthasastra'"
        ),
        TimelineEvent(
            id = "time_5",
            period = "c. 499 CE",
            eraName = "Gupta Golden Age",
            title = "Aryabhata I: The Aryabhatiya",
            titleHi = "आर्यभट्ट प्रथम: आर्यभटीय ग्रंथ",
            description = "At age 23, Aryabhata calculates Pi as 3.1416, invents the Kuttaka indeterminate algorithm, creates sine trigonometric tables, and posits the diurnal axial rotation of planet Earth.",
            domain = "Mathematics & Astronomy",
            scholars = listOf("Aryabhata I"),
            primaryTexts = listOf("Aryabhatiya"),
            sourceCitation = "W.E. Clark, 'The Aryabhatiya of Aryabhata'"
        ),
        TimelineEvent(
            id = "time_6",
            period = "c. 628 CE",
            eraName = "Classical Astronomy & Algebra",
            title = "Brahmagupta: Brahmasphutasiddhanta",
            titleHi = "ब्रह्मगुप्त: ब्राह्मस्फुटसिद्धान्त एवं शून्य के नियम",
            description = "In Ujjain, Brahmagupta formalizes arithmetic operations for zero and negative quantities, solves second-order indeterminate equations (Pell's equation: Varga-prakriti), and establishes cyclic quadrilateral cyclic area formulas.",
            domain = "Indian Mathematics",
            scholars = listOf("Brahmagupta"),
            primaryTexts = listOf("Brahmasphutasiddhanta"),
            sourceCitation = "INSA History of Science Division"
        ),
        TimelineEvent(
            id = "time_7",
            period = "c. 1150 CE",
            eraName = "Medieval Mathematical Zenith",
            title = "Bhaskara II: Siddhanta Shiromani & Lilavati",
            titleHi = "भास्कराचार्य द्वितीय: सिद्धान्त शिरोमणि एवं लीलावती",
            description = "Bhaskara II invents the Chakravala cyclic method for Diophantine equations, notes that division by zero yields an infinite magnitude (Khahara), and conceptualizes early differential calculus (Tatkalika-gati).",
            domain = "Mathematics & Astronomy",
            scholars = listOf("Bhaskara II (Bhaskaracharya)"),
            primaryTexts = listOf("Lilavati", "Bijaganita", "Siddhanta Shiromani"),
            sourceCitation = "Colebrooke, 'Algebra with Arithmetic and Mensuration'"
        ),
        TimelineEvent(
            id = "time_8",
            period = "c. 1350–1550 CE",
            eraName = "Kerala School of Mathematics",
            title = "Madhava & The Calculus of Infinite Series",
            titleHi = "माधव एवं अनन्त श्रेणियों का कलन",
            description = "Madhava of Sangamagrama, Nilakantha, and Jyeshthadeva derive infinite power series for sine, cosine, and arctangent centuries prior to Newton, Gregory, and Leibniz, documenting them in the Malayalam treatise Yuktibhasha.",
            domain = "Indian Mathematics",
            scholars = listOf("Madhava of Sangamagrama", "Nilakantha Somayaji", "Jyeshthadeva"),
            primaryTexts = listOf("Yuktibhasha", "Tantrasamgraha"),
            sourceCitation = "Hindustan Book Agency / Springer (2008)"
        ),
        TimelineEvent(
            id = "time_9",
            period = "c. 1724–1734 CE",
            eraName = "Late Classical Astronomy",
            title = "Maharaja Sawai Jai Singh II: Jantar Mantar Observatories",
            titleHi = "महाराजा सवाई जयसिंह द्वितीय: जन्तर-मन्तर वेधशालाएं",
            description = "Construction of colossal masonry astronomical observatories in Delhi, Jaipur, Ujjain, Varanasi, and Mathura, featuring the Samrat Yantra: the world's largest stone sundial, measuring time to 2 seconds precision.",
            domain = "Indian Astronomy",
            scholars = listOf("Maharaja Sawai Jai Singh II", "Pandit Jagannatha Samrat"),
            primaryTexts = listOf("Zij-i Muhammad Shahi", "Siddhanta Samrat"),
            sourceCitation = "UNESCO World Heritage Inscriptions"
        )
    )

    val knowledgeNodes = listOf(
        KnowledgeNode(
            id = "node_math",
            label = "Indian Mathematics (Ganita)",
            labelHi = "भारतीय गणित",
            category = "Domain",
            description = "Discipline encompassing arithmetic, geometry, algebra, and infinite series.",
            connectedNodeIds = listOf("node_sulba", "node_zero", "node_aryabhata", "node_madhava", "node_astro"),
            xOffset = 0.5f,
            yOffset = 0.2f
        ),
        KnowledgeNode(
            id = "node_astro",
            label = "Indian Astronomy (Jyotisha)",
            labelHi = "भारतीय खगोलशास्त्र",
            category = "Domain",
            description = "Observational tracking of planets, Nakshatras, and solar-lunar calendars.",
            connectedNodeIds = listOf("node_math", "node_aryabhata", "node_varaha", "node_jantar"),
            xOffset = 0.8f,
            yOffset = 0.3f
        ),
        KnowledgeNode(
            id = "node_ayur",
            label = "Ayurveda",
            labelHi = "आयुर्वेद",
            category = "Domain",
            description = "Science of longevity, Tridosha physiology, and clinical surgery.",
            connectedNodeIds = listOf("node_charaka", "node_sushruta", "node_botany"),
            xOffset = 0.2f,
            yOffset = 0.35f
        ),
        KnowledgeNode(
            id = "node_phil",
            label = "Philosophy (Darshana)",
            labelHi = "दर्शन शास्त्र",
            category = "Domain",
            description = "The six classical Astika schools and logic epistemology.",
            connectedNodeIds = listOf("node_nyaya", "node_yoga", "node_linguistics"),
            xOffset = 0.3f,
            yOffset = 0.7f
        ),
        KnowledgeNode(
            id = "node_sulba",
            label = "Sulba Sutras",
            labelHi = "शुल्ब सूत्र",
            category = "Text",
            description = "Vedic manuals for altar construction establishing early geometry.",
            connectedNodeIds = listOf("node_math", "node_baudhayana", "node_pythagoras"),
            xOffset = 0.4f,
            yOffset = 0.08f
        ),
        KnowledgeNode(
            id = "node_zero",
            label = "Zero (Shunya)",
            labelHi = "शून्य",
            category = "Concept",
            description = "Number, operator, and foundational pillar of decimal place-value notation.",
            connectedNodeIds = listOf("node_math", "node_brahma"),
            xOffset = 0.6f,
            yOffset = 0.08f
        ),
        KnowledgeNode(
            id = "node_aryabhata",
            label = "Aryabhata I",
            labelHi = "आर्यभट्ट प्रथम",
            category = "Scholar",
            description = "Author of Aryabhatiya; computed Pi = 3.1416, trigonometry, and planetary rotations.",
            connectedNodeIds = listOf("node_math", "node_astro"),
            xOffset = 0.65f,
            yOffset = 0.32f
        ),
        KnowledgeNode(
            id = "node_baudhayana",
            label = "Baudhayana",
            labelHi = "बौधायन",
            category = "Scholar",
            description = "Geometrician who formulated the diagonal theorem for rectangles.",
            connectedNodeIds = listOf("node_sulba"),
            xOffset = 0.25f,
            yOffset = 0.08f
        ),
        KnowledgeNode(
            id = "node_brahma",
            label = "Brahmagupta",
            labelHi = "ब्रह्मगुप्त",
            category = "Scholar",
            description = "Head of Ujjain observatory; formalized arithmetic operations of zero.",
            connectedNodeIds = listOf("node_zero", "node_math"),
            xOffset = 0.75f,
            yOffset = 0.15f
        ),
        KnowledgeNode(
            id = "node_madhava",
            label = "Madhava of Sangamagrama",
            labelHi = "संगमग्राम के माधव",
            category = "Scholar",
            description = "Founder of the Kerala School; developed infinite power series for calculus.",
            connectedNodeIds = listOf("node_math", "node_astro"),
            xOffset = 0.45f,
            yOffset = 0.38f
        ),
        KnowledgeNode(
            id = "node_sushruta",
            label = "Acharya Sushruta",
            labelHi = "आचार्य सुश्रुत",
            category = "Scholar",
            description = "Pioneered over 120 surgical tools and living cheek flap rhinoplasty.",
            connectedNodeIds = listOf("node_ayur"),
            xOffset = 0.12f,
            yOffset = 0.5f
        ),
        KnowledgeNode(
            id = "node_charaka",
            label = "Acharya Charaka",
            labelHi = "आचार्य चरक",
            category = "Scholar",
            description = "Codifier of internal medicine, etiology, and Tridosha pathology.",
            connectedNodeIds = listOf("node_ayur"),
            xOffset = 0.15f,
            yOffset = 0.25f
        ),
        KnowledgeNode(
            id = "node_nyaya",
            label = "Nyaya Logic",
            labelHi = "न्याय तर्क",
            category = "Concept",
            description = "Epistemological system with 4 Pramanas and 5-step syllogism.",
            connectedNodeIds = listOf("node_phil", "node_linguistics"),
            xOffset = 0.32f,
            yOffset = 0.85f
        ),
        KnowledgeNode(
            id = "node_linguistics",
            label = "Paninian Grammar",
            labelHi = "पाणिनीय व्याकरण",
            category = "Domain",
            description = "Ashtadhyayi: Generative algorithmic grammar anticipating BNF computer science.",
            connectedNodeIds = listOf("node_phil", "node_math"),
            xOffset = 0.55f,
            yOffset = 0.65f
        ),
        KnowledgeNode(
            id = "node_jantar",
            label = "Jantar Mantar",
            labelHi = "जन्तर-मन्तर",
            category = "Innovation",
            description = "Giant stone observatories measuring solar and stellar coordinates.",
            connectedNodeIds = listOf("node_astro"),
            xOffset = 0.88f,
            yOffset = 0.48f
        ),
        KnowledgeNode(
            id = "node_metal",
            label = "Metallurgy (Dhatu Vada)",
            labelHi = "धातुकर्म",
            category = "Domain",
            description = "Wootz crucible steel, zinc smelting, rustless Delhi Iron Pillar.",
            connectedNodeIds = listOf("node_math", "node_astro"),
            xOffset = 0.75f,
            yOffset = 0.75f
        )
    )

    val achievements = listOf(
        Achievement(
            id = "ach_1",
            title = "First Steps in IKS",
            description = "Completed your first lesson in Indian Knowledge Systems.",
            iconName = "School",
            xpReward = 100,
            isUnlocked = true,
            unlockedAt = "2026-09-22"
        ),
        Achievement(
            id = "ach_2",
            title = "7-Day Vidya Streak",
            description = "Maintained an unbroken 7-day learning habit.",
            iconName = "LocalFireDepartment",
            xpReward = 250,
            isUnlocked = true,
            unlockedAt = "2026-09-28"
        ),
        Achievement(
            id = "ach_3",
            title = "Ganita Scholar",
            description = "Score 100% in an Indian Mathematics quiz.",
            iconName = "Calculate",
            xpReward = 150,
            isUnlocked = true,
            unlockedAt = "2026-09-27"
        ),
        Achievement(
            id = "ach_4",
            title = "IKS Explorer",
            description = "Studied lessons from at least 3 distinct knowledge domains.",
            iconName = "Explore",
            xpReward = 200,
            isUnlocked = true,
            unlockedAt = "2026-09-26"
        ),
        Achievement(
            id = "ach_5",
            title = "Ayurveda Practitioner",
            description = "Mastered the Tridosha and Sushruta lessons.",
            iconName = "Healing",
            xpReward = 150,
            isUnlocked = false
        ),
        Achievement(
            id = "ach_6",
            title = "Master of Logic",
            description = "Completed the Nyaya Epistemology assessment with full marks.",
            iconName = "Psychology",
            xpReward = 300,
            isUnlocked = false
        ),
        Achievement(
            id = "ach_7",
            title = "Flashcard Prodigy",
            description = "Reviewed 20 flashcards with full mastery.",
            iconName = "Style",
            xpReward = 150,
            isUnlocked = false
        ),
        Achievement(
            id = "ach_8",
            title = "Course Completer",
            description = "Finished all lessons and quizzes of an entire course.",
            iconName = "EmojiEvents",
            xpReward = 500,
            isUnlocked = false
        )
    )

    val dailyFact = DailyIksFact(
        date = "29 September 2026",
        factText = "In 499 CE, Aryabhata calculated the value of π as 3.1416 and explicitly designated it as 'asanna' (approximative), anticipating the mathematical concept of irrational numbers by over a millennium.",
        factSource = "Aryabhatiya, Ganitapada, Verse 10 (Critical Edition, INSA)",
        conceptTitle = "The Geometry of the Sulba Sutras",
        conceptSummary = "Baudhayana formulated the diagonal theorem for rectangles centuries before Pythagoras, enabling exact ritual altar construction with integer triples.",
        figureName = "Aryabhata I (476–550 CE)",
        figureRole = "Astronomer & Mathematician at Kusumapura (Nalanda)",
        figureEra = "Classical Gupta Era",
        dailyQuestion = quizQuestions[0]
    )

    val defaultStudyPlan = listOf(
        StudyPlanItem("sp_1", "Monday", 20, "Indian Mathematics: Sulba Geometry", "Lesson Reading", true),
        StudyPlanItem("sp_2", "Monday", 10, "Sulba & Vedic Mathematics", "Flashcards", true),
        StudyPlanItem("sp_3", "Tuesday", 20, "Indian Astronomy: 27 Nakshatras", "Lesson Reading", false),
        StudyPlanItem("sp_4", "Tuesday", 10, "Astronomy Coordinates", "Practice Quiz", false),
        StudyPlanItem("sp_5", "Wednesday", 20, "Ayurveda: Tridosha Physiology", "Lesson Reading", false),
        StudyPlanItem("sp_6", "Thursday", 25, "Indian Philosophy: Nyaya Logic", "Lesson Reading", false),
        StudyPlanItem("sp_7", "Friday", 15, "Classical Sciences Synthesis", "Revision & Quiz", false)
    )
}
