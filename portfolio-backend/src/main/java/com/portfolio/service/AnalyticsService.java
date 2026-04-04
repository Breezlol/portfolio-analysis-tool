package com.portfolio.service;

import com.portfolio.datastructure.AVLTree;
import com.portfolio.datastructure.MinHeap;
import com.portfolio.entity.PortfolioItem;
import com.portfolio.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Identifies top-k gainers and losers using two MinHeap instances.
 *
 * <p>Algorithm: O(n log k) — for each holding insert into a size-k heap,
 * evicting the weakest candidate when the heap exceeds k. Full sort would
 * be O(n log n); this approach is preferable when k &lt;&lt; n.
 */
@Service
public class AnalyticsService {

