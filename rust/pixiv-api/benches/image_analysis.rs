use criterion::{Criterion, black_box, criterion_group, criterion_main};
use palleria_pixiv_api::{analyze_rgba, average_luminance_rgba, dominant_argb_rgba};

fn rgba_sample(width: usize, height: usize) -> Vec<u8> {
    (0..width * height)
        .flat_map(|index| {
            let red = (index.wrapping_mul(17) & 0xff) as u8;
            let green = (index.wrapping_mul(29) & 0xff) as u8;
            let blue = (index.wrapping_mul(43) & 0xff) as u8;
            [red, green, blue, 255]
        })
        .collect()
}

fn analyze_image_samples(criterion: &mut Criterion) {
    let luminance_sample = rgba_sample(40, 40);
    criterion.bench_function("image luminance/40x40", |bencher| {
        bencher.iter(|| average_luminance_rgba(black_box(luminance_sample.clone())));
    });
    criterion.bench_function("image combined/40x40", |bencher| {
        bencher.iter(|| analyze_rgba(black_box(luminance_sample.clone())));
    });

    let color_sample = rgba_sample(32, 32);
    criterion.bench_function("image dominant color/32x32", |bencher| {
        bencher.iter(|| dominant_argb_rgba(black_box(color_sample.clone())));
    });
    criterion.bench_function("image combined/32x32", |bencher| {
        bencher.iter(|| analyze_rgba(black_box(color_sample.clone())));
    });
}

criterion_group!(benches, analyze_image_samples);
criterion_main!(benches);
