# OPPO 음력 한국어 변환 - LSPosed 실험 모듈

## 현재 상태
- 업로드된 OPPO Calendar APK의 DEX에서 `com/coloros/calendar`와 `COUILunarUtil` 관련 심볼을 확인했습니다.
- 이 프로젝트는 `com.coloros.calendar` 프로세스 안의 `TextView.setText()`를 후킹하고, 중국어 음력 날짜로 보이는 문자열만 제한적으로 한국어로 바꾸는 **초기 실험판 소스**입니다.
- 모든 화면/위젯/커스텀 렌더러에서 작동한다고 검증된 완성판은 아닙니다. 특히 날짜 문자열이 TextView가 아닌 Compose/Canvas/커스텀 View에서 그려지면 추가 후킹이 필요합니다.

## 빌드
GitHub 저장소에 프로젝트 파일을 올린 뒤 Actions → Build LSPosed module → Run workflow를 실행합니다. 완료된 실행의 Artifacts에서 `OppoLunarKorean-debug`를 내려받습니다.

## 설치
1. APK 설치
2. LSPosed에서 `OPPO 음력 한국어 변환` 모듈 활성화
3. 적용 대상에서 OPPO 캘린더만 선택 (`com.coloros.calendar`)
4. 캘린더 강제 종료 후 다시 실행

## 주의
- 설치 전 캘린더 데이터 백업을 권장합니다.
- 첫 버전은 `农历`, `初一`~`初十`, `十一`~`十九`, `廿一`~`廿九`, `卅一`, 중국식 월 이름처럼 명확한 음력 표식을 포함한 TextView 문자열만 처리합니다.
- 모듈을 비활성화하거나 제거하면 원상복구됩니다.
- 이 소스는 아직 실제 기기에서 빌드/런타임 검증되지 않았습니다. 적용 후 화면이 바뀌지 않으면 해당 음력 표시의 실제 View/메서드를 추가 분석해야 합니다.
