-- Public demo product data only. Do not put credentials or tokens in this file.
-- Re-running this script updates the nine demo products and rebuilds their colors/sizes.

SET NAMES utf8mb4;

INSERT INTO product (
    id,
    product_code,
    name,
    category,
    material,
    features,
    price,
    thumbnail_url,
    brand_official_description,
    ai_generated_description
) VALUES
(
    'prod_1',
    'MWHGAXT03CO001',
    'Tracy 비세토스 호보',
    '호보백',
    '비세토스 모노그램 캔버스 + 나파 가죽',
    '길이 조절 및 탈부착 가능한 가죽 스트랩, 로고 락 클로저, 내부 슬립 포켓, 지퍼 수납공간',
    1690000,
    '/demo/products/prod_1/front.png',
    '로고 락 클로저와 나파 가죽 트림이 돋보이는 비세토스 모노그램 캔버스 호보백입니다. 마이크로파이버 스웨이드 안감과 내부 수납공간을 갖췄습니다.',
    '클래식한 모노그램과 가죽 트림이 돋보이는 포멀한 호보백입니다. 조절 가능한 스트랩으로 데일리 숄더백으로 활용할 수 있습니다.'
),
(
    'prod_2',
    'MWPGSMT04BK001',
    'Toni 맥시 모노그램 레더 탑 지퍼 쇼퍼',
    '쇼퍼백',
    '천연 풀그레인 레더',
    '탈부착 및 길이 조절 가능한 가죽 스트랩, 가죽 탑 핸들, 지퍼 클로저, 내부 포켓',
    1290000,
    '/demo/products/prod_2/front.png',
    '엠보싱 처리된 맥시 비세토스 모노그램 모티프가 돋보이는 이탈리아산 풀그레인 레더 쇼퍼입니다. 토트백과 숄더백으로 연출할 수 있습니다.',
    '블랙 레더와 심플한 실루엣이 미니멀하고 클래식한 인상을 주며 포멀한 스타일에도 활용하기 좋은 2way 쇼퍼백입니다.'
),
(
    'prod_3',
    'MWHFSTA03CO001',
    'Aren 비세토스 듀오 호보',
    '호보백',
    '비세토스 모노그램 캔버스 + 천연 나파 가죽',
    '두 개의 백을 결합하거나 분리할 수 있는 투인원 디자인, 조절 가능한 가죽 스트랩, 체인 핸들, 지퍼 클로저',
    1290000,
    '/demo/products/prod_3/front.png',
    '비세토스 캔버스와 천연 나파 가죽으로 제작한 두 개의 호보백을 결합하거나 단독으로 사용할 수 있는 투인원 제품입니다.',
    '체인 디테일과 투인원 구성이 트렌디하며, 모노그램과 가죽 소재가 클래식한 분위기를 더하는 호보백입니다.'
),
(
    'prod_4',
    'MWTGATA01CO001',
    'Pina 비세토스 스터드 장식 토트',
    '토트백',
    '비세토스 모노그램 캔버스 + 나파 가죽',
    '탈부착 가능한 가죽 스트랩, 가죽 핸들, 메탈 스터드, 양방향 지퍼, 내부 슬립 포켓 및 지퍼 수납공간',
    1850000,
    '/demo/products/prod_4/front.png',
    '나파 가죽 트림과 메탈 스터드 장식이 돋보이는 비세토스 모노그램 캔버스 토트백입니다. 내부 포켓과 메탈 피트를 갖췄습니다.',
    '볼드한 메탈 스터드와 로고 디테일이 스트릿하고 트렌디한 인상을 주며, 넉넉한 수납이 가능한 토트백입니다.'
),
(
    'prod_5',
    'MWHESTA01CO001',
    'Aren 비세토스 호보',
    '호보백',
    '비세토스 모노그램 캔버스 + 나파 가죽',
    '조절 가능한 가죽 숄더 스트랩, 탈부착 가능한 가죽 행택과 금속 자물쇠, 지퍼 클로저, 내부 슬립 포켓',
    1290000,
    '/demo/products/prod_5/front.png',
    '여유로운 클래식 실루엣에 조절 가능한 가죽 스트랩과 가죽 행택, 로고 금속 자물쇠를 더한 스몰 호보백입니다.',
    '부드러운 실루엣과 데일리 수납을 갖춘 캐주얼한 호보백이며, 모노그램과 가죽 장식으로 클래식한 분위기를 냅니다.'
),
(
    'prod_6',
    'MMVGATT08CO001',
    'MCM X We The Best Ottomar 비세토스 위켄더 백',
    '위켄더백',
    '비세토스 모노그램 캔버스 + 나파 가죽',
    '탈부착 및 길이 조절 가능한 패브릭 스트랩, 가죽 탑 핸들, 양방향 지퍼, 내부 수납공간, 캐리어 밴드, 폴더블 디자인',
    2290000,
    '/demo/products/prod_6/front.png',
    'MCM과 We The Best 협업으로 완성한 비세토스 위켄더 백입니다. 블루톤 나파 가죽 디테일과 탈착 가능한 참을 갖췄습니다.',
    '볼드한 협업 로고와 컬러 디테일이 스트릿하고 트렌디한 여행 스타일에 어울리며, 넉넉한 수납이 가능한 위켄더 백입니다.'
),
(
    'prod_7',
    'MWPGSLR03I8001',
    'New Liz 비세토스 쇼퍼',
    '쇼퍼백',
    '비세토스 모노그램 캔버스 + 레더 트림',
    '길어진 가죽 스트랩, 후크 잠금, 탈착 가능한 지퍼 파우치, D링 참 디테일, 레더 바닥 패드',
    1050000,
    '/demo/products/prod_7/front.png',
    '매칭 지퍼 파우치와 가죽 트림이 더해진 가볍고 부드러운 비세토스 모노그램 캔버스 쇼퍼백입니다.',
    '가볍고 편안한 실루엣과 탈착 파우치가 데일리 캐주얼 스타일에 적합하며, 베이지와 블랙 조합이 차분한 인상을 줍니다.'
),
(
    'prod_8',
    'MWKGATA03PZ001',
    'Aren 비세토스 드로우스트링 백팩',
    '백팩',
    '비세토스 모노그램 캔버스 + 레더 트림',
    '길이 조절 가능한 숄더 스트랩, 가죽 탑 핸들, 마그네틱 스냅과 드로우스트링, 앞면 플랩 포켓, 내부 슬립 포켓',
    1650000,
    '/demo/products/prod_8/front.png',
    '소프트 핑크 비세토스 캔버스와 가죽으로 완성한 드로우스트링 백팩으로, 버클 장식과 다양한 수납공간이 특징입니다.',
    '소프트 핑크 컬러와 버클 장식이 트렌디하며, 조절 가능한 스트랩과 여러 포켓으로 캐주얼한 데일리 스타일에 적합합니다.'
),
(
    'prod_9',
    'MMLGATA02BK001',
    'Aren 엠보스드 모노그램 레더 슬링백',
    '슬링백',
    '나파 레더',
    '길이 조절 가능한 패브릭 웨빙 스트랩, 엠보스드 모노그램, 앞면 지퍼 포켓, 양방향 지퍼, 내부 포켓',
    1550000,
    '/demo/products/prod_9/front.png',
    '나파 가죽에 비세토스 모노그램을 엠보싱으로 표현한 스몰 슬링백으로, 싱글 스트랩과 지퍼 수납공간을 갖췄습니다.',
    '블랙 레더와 자유로운 움직임을 고려한 싱글 스트랩이 미니멀하고 스트릿한 인상을 주는 슬링백입니다.'
)
ON DUPLICATE KEY UPDATE
    product_code = VALUES(product_code),
    name = VALUES(name),
    category = VALUES(category),
    material = VALUES(material),
    features = VALUES(features),
    price = VALUES(price),
    thumbnail_url = VALUES(thumbnail_url),
    brand_official_description = VALUES(brand_official_description),
    ai_generated_description = VALUES(ai_generated_description);

DELETE FROM product_color
WHERE product_id IN ('prod_1', 'prod_2', 'prod_3', 'prod_4', 'prod_5', 'prod_6', 'prod_7', 'prod_8', 'prod_9');

INSERT INTO product_color (product_id, color) VALUES
('prod_1', 'Cognac'),
('prod_2', 'Black'),
('prod_3', 'Cognac'),
('prod_4', 'Cognac'),
('prod_5', 'Cognac'),
('prod_6', 'Cognac'),
('prod_7', 'Beige + Black'),
('prod_8', 'Soft Pink'),
('prod_9', 'Black');

DELETE FROM product_size
WHERE product_id IN ('prod_1', 'prod_2', 'prod_3', 'prod_4', 'prod_5', 'prod_6', 'prod_7', 'prod_8', 'prod_9');

INSERT INTO product_size (product_id, size) VALUES
('prod_1', 'L'),
('prod_1', '11 x 33 x 31 cm'),
('prod_2', '미니'),
('prod_2', '9 x 19 x 19 cm'),
('prod_3', 'S'),
('prod_3', '6 x 26 x 22 cm'),
('prod_4', 'L'),
('prod_4', '18 x 41 x 32 cm'),
('prod_5', 'S'),
('prod_5', '10 x 26 x 19 cm'),
('prod_6', '50.5 cm / 19.9 인치'),
('prod_6', '23 x 51 x 29 cm'),
('prod_7', 'S'),
('prod_7', '15 x 25 x 31 cm'),
('prod_8', 'S'),
('prod_8', '13 x 29 x 32 cm'),
('prod_9', 'S'),
('prod_9', '9 x 21 x 37 cm');
