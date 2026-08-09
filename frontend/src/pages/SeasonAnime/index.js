// src/pages/season-anime/SeasonAnime.jsx
import {useCallback, useEffect, useMemo, useState} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import PageTemplate from "../../components/layout/PageTemplate";
import SeasonTab from "./components/SeasonTab";
import AnimeList from "./components/AnimeList";
import CommonSelect from "../../components/base/CommonSelect";
import {toAirStatusLabel, toTypeLabel} from "../../utils/strUtil";
import {SelectWrapper} from "./components/styles/SeasonTab.style";
import {
    getCurrentSeason,
    getCurrentSeasonYear,
    getPreviousSeasonFromSeason,
    getPreviousSeasonYearFromYearAndSeason,
} from "../../utils/dateUtil";

const LOCAL_STORAGE_KEY = "seasonFilters";
const PAGE_SIZE = 12;

const getInitialFilters = () => {
    const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
    if (saved) {
        try {
            const parsed = JSON.parse(saved);
            return {
                sortBy: parsed.sortBy || "score",
                filterBy: parsed.filterBy || {type: "tv", airStatus: "all"},
            };
        } catch {
        }
    }
    return {
        sortBy: "score",
        filterBy: {type: "tv", airStatus: "all"},
    };
};

const SeasonAnime = () => {
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const yearParam = queryParams.get("year");
    const seasonParam = queryParams.get("season");

    const [activeTab, setActiveTab] = useState("4");
    const [page, setPage] = useState(1);
    const [filters, setFilters] = useState(getInitialFilters);
    const {sortBy, filterBy} = filters;

    const currentSeasonInfo = useMemo(() => {
        let baseSeason = seasonParam;
        let baseYear = yearParam;

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

        const targetIndex = parseInt(activeTab, 10) - 1;
        if (targetIndex >= 0 && targetIndex < seasons.length) {
            return seasons[targetIndex];
        }
        return seasons[3];
    }, [seasonParam, yearParam, activeTab]);

    useEffect(() => {
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify({sortBy, filterBy}));
    }, [sortBy, filterBy]);

    useEffect(() => {
        window.scrollTo(0, 0);
    }, [page]);

    const handleTypeChange = (value) => {
        setFilters((prev) => ({...prev, filterBy: {...prev.filterBy, type: value}}));
        setPage(1);
    };

    const handleAirStatusChange = (value) => {
        setFilters((prev) => ({...prev, filterBy: {...prev.filterBy, airStatus: value}}));
        setPage(1);
    };

    const handleSortByChange = (value) => {
        setFilters((prev) => ({...prev, sortBy: value}));
    };

    return (
        <PageTemplate>
            <SelectWrapper>
                <CommonSelect
                    value={`Type: ${toTypeLabel(filterBy.type)}`}
                    onChange={handleTypeChange}
                >
                    <CommonSelect.Option value="tv">TV</CommonSelect.Option>
                </CommonSelect>

                <CommonSelect
                    value={`Air Status: ${toAirStatusLabel(filterBy.airStatus)}`}
                    onChange={handleAirStatusChange}
                >
                    <CommonSelect.Option value="all">All</CommonSelect.Option>
                    <CommonSelect.Option value="currently_airing">Airing</CommonSelect.Option>
                    <CommonSelect.Option value="finished_airing">Ended</CommonSelect.Option>
                </CommonSelect>

                <CommonSelect
                    value={`Sort: ${sortBy.charAt(0).toUpperCase() + sortBy.slice(1)}`}
                    onChange={handleSortByChange}
                >
                    <CommonSelect.Option value="score">Score</CommonSelect.Option>
                    <CommonSelect.Option value="votes">Votes</CommonSelect.Option>
                    <CommonSelect.Option value="rank">Rank</CommonSelect.Option>
                    <CommonSelect.Option value="members">Members</CommonSelect.Option>
                    <CommonSelect.Option value="popularity">Popularity</CommonSelect.Option>
                </CommonSelect>
            </SelectWrapper>

            <SeasonTab
                season={seasonParam}
                year={yearParam}
                activeTab={activeTab}
                setActiveTab={setActiveTab}
                setPage={setPage}
            />

            <AnimeList
                year={currentSeasonInfo.year}
                season={currentSeasonInfo.season}
                sortBy={sortBy}
                filterBy={filterBy}
                page={page}
                setPage={setPage}
                pageSize={PAGE_SIZE}
                selected={true}
            />
        </PageTemplate>
    );
};

export default SeasonAnime;