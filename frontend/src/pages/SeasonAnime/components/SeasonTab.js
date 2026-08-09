import {useMemo} from "react";
import {useNavigate} from "react-router-dom";
import {
    getCurrentSeason,
    getCurrentSeasonYear,
    getNextSeasonFromSeason,
    getNextSeasonYearFromYearAndSeason,
    getPreviousSeasonFromSeason,
    getPreviousSeasonYearFromYearAndSeason,
} from "../../../utils/dateUtil";
import {CustomTabs, StyledSeasonTab} from "./styles/SeasonTab.style";

const SeasonTab = ({season, year, activeTab, setActiveTab, setPage}) => {
    const navigate = useNavigate();

    const seasonData = useMemo(() => {
        let baseSeason = season;
        let baseYear = year;

        if (baseSeason == null || baseYear == null) {
            baseSeason = getCurrentSeason();
            baseYear = getCurrentSeasonYear();
        } else {
            baseYear = parseInt(baseYear, 10);
        }

        const seasons = [{season: baseSeason, year: baseYear}];

        let currSeason = baseSeason;
        let currYear = baseYear;
        for (let i = 0; i < 3; i++) {
            const prevS = getPreviousSeasonFromSeason(currSeason);
            const prevY = parseInt(getPreviousSeasonYearFromYearAndSeason(currYear, currSeason), 10);
            seasons.unshift({season: prevS, year: prevY});
            currSeason = prevS;
            currYear = prevY;
        }

        return seasons;
    }, [season, year]);

    const handleTabChange = (key) => {
        const targetIndex = parseInt(key, 10) - 1;

        if (targetIndex >= 0 && targetIndex < seasonData.length) {
            const target = seasonData[targetIndex];
            navigate(`/season-anime?year=${target.year}&season=${target.season}`);
            setPage(1);
        } else if (key === "prev") {
            const firstTarget = seasonData[0];
            const nextSeason = getPreviousSeasonFromSeason(firstTarget.season);
            const nextYear = getPreviousSeasonYearFromYearAndSeason(
                firstTarget.year,
                firstTarget.season
            );
            navigate(`/season-anime?year=${nextYear}&season=${nextSeason}`);
            setPage(1);
        } else if (key === "next") {
            const lastTarget = seasonData[seasonData.length - 1];
            const nextSeason = getNextSeasonFromSeason(lastTarget.season);
            const nextYear = getNextSeasonYearFromYearAndSeason(
                lastTarget.year,
                lastTarget.season
            );
            navigate(`/season-anime?year=${nextYear}&season=${nextSeason}`);
            setPage(1);
        } else if (key === "current") {
            const currentSeason = getCurrentSeason();
            const currentYear = getCurrentSeasonYear();
            navigate(`/season-anime?year=${currentYear}&season=${currentSeason}`);
            setPage(1);
        } else if (key === "archive") {
            navigate("/season-archive");
        } else {
            setActiveTab(key);
            setPage(1);
        }
    };

    const tabs = useMemo(() => {
        return [
            {key: "prev", label: "...", content: null},
            ...seasonData.map(({season: itemSeason, year: itemYear}, index) => ({
                key: `${index + 1}`,
                label: `${itemSeason.charAt(0).toUpperCase() + itemSeason.slice(1)} ${itemYear}`,
                content: null, // 탭 내부 content는 비워둠 (아래쪽 AnimeList에서 처리)
            })),
            {key: "next", label: "...", content: null},
            {key: "current", label: "Current", content: null},
            {key: "archive", label: "Archive", content: null},
        ];
    }, [seasonData]);

    return (
        <StyledSeasonTab>
            <CustomTabs
                tabs={tabs}
                defaultActiveKey="4"
                activeKey={activeTab}
                onChange={handleTabChange}
            />
        </StyledSeasonTab>
    );
};

export default SeasonTab;